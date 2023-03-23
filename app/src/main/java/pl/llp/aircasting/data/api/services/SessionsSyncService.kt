package pl.llp.aircasting.data.api.services

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.core.net.toUri
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.params.SyncSessionBody
import pl.llp.aircasting.data.api.params.SyncSessionParams
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.NoteRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.LogoutEvent
import pl.llp.aircasting.util.events.SessionsSyncErrorEvent
import pl.llp.aircasting.util.events.SessionsSyncEvent
import pl.llp.aircasting.util.events.SessionsSyncSuccessEvent
import pl.llp.aircasting.util.exceptions.DBInsertException
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SyncError
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError
import pl.llp.aircasting.util.extensions.encodeToBase64
import pl.llp.aircasting.util.extensions.safeRegister
import java.util.concurrent.atomic.AtomicBoolean

class SessionsSyncService private constructor(
    private val apiService: ApiService,
    private val errorHandler: ErrorHandler,
    private val settings: Settings
) {
    init {
        EventBus.getDefault().safeRegister(this)
    }

    companion object {
        private var mSingleton: SessionsSyncService? = null

        fun get(
            apiService: ApiService,
            errorHandler: ErrorHandler,
            settings: Settings
        ): SessionsSyncService {
            if (mSingleton == null) {
                mSingleton = SessionsSyncService(apiService, errorHandler, settings)
            }

            return mSingleton!!
        }

        fun destroy() {
            mSingleton?.destroy()
            mSingleton = null
        }
    }

    private val uploadService: MobileSessionUploadService =
        MobileSessionUploadService(apiService)
    private val downloadService: SessionDownloadService =
        SessionDownloadService(apiService, errorHandler)
    private val removeOldMeasurementsService: RemoveOldMeasurementsService =
        RemoveOldMeasurementsService()

    private val sessionRepository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val noteRepository = NoteRepository()
    private val gson = Gson()
    private var syncInBackground = AtomicBoolean(false)
    private val syncAfterDeletion = AtomicBoolean(false)
    private var triedToSyncBackground = AtomicBoolean(false)
    private var syncJob: Job? = null

    @Subscribe
    fun onMessageEvent(logout: LogoutEvent) {
        if (logout.inProgress && !logout.afterAccountDeletion)
            sync()
    }

    fun destroy() {
        syncJob?.cancel()
    }

    // Define a sealed class to represent the result of the sync operation
    sealed class SyncResult {
        object Success : SyncResult()
        data class Error(val throwable: Throwable?) : SyncResult()
    }

    suspend fun syncSuspendNoFlow(): Result<SyncResult> = withContext(Dispatchers.IO) {
        val sessions = sessionRepository.allSessionsExceptRecording()
        val syncParams = sessions.map { session -> SyncSessionParams(session) }
        val jsonData = gson.toJson(syncParams)

        val syncResult = runCatching {
            val response = apiService.sync(SyncSessionBody(jsonData))

            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    Log.d(TAG, "Updating local sessions from SyncSuspend")
                    delete(body.deleted)
                    removeOldMeasurements()

                    upload(body.upload)
                    download(body.download)
                }
                SyncResult.Success
            } else {
                val exception = Exception("Sync failed with response code ${response.code()}")
                handleSyncError(exception)

                SyncResult.Error(exception)
            }
        }

        syncResult.onFailure { exception ->
            handleSyncError(exception)
        }

        syncResult
    }

    fun sync() {
        CoroutineScope(Dispatchers.IO).launch {
            // This will happen if we regain connectivity when app is in background.
            // When in foreground again, it should sync
            if (syncInBackground.get()) {
                triedToSyncBackground.set(true)
            }

            if (syncInBackground.get()) {
                Log.d(
                    TAG, "Not performing sync:\nsyncInBackground = ${syncInBackground.get()}"
                )
                return@launch
            }

            syncJob = CoroutineScope(Dispatchers.IO).launch {
                // TODO: for backward compatibility, remove later
                EventBus.getDefault().postSticky(SessionsSyncEvent())

                val sessions = sessionRepository.allSessionsExceptRecording()
                val syncParams = sessions.map { session -> SyncSessionParams(session) }
                val jsonData = gson.toJson(syncParams)

                try {
                    val response = apiService.sync(SyncSessionBody(jsonData))

                    if (response.isSuccessful) {
                        val body = response.body()
                        body?.let {
                            delete(body.deleted)
                            removeOldMeasurements()

                            upload(body.upload)
                            download(body.download)
                            // TODO: for backward compatibility, remove later
                            EventBus.getDefault().post(SessionsSyncSuccessEvent())
                        }
                    } else handleSyncError()

                } catch (t: Throwable) {
                    handleSyncError(t)
                }
                setSyncStateToFinished()
            }
        }
    }


    private suspend fun removeOldMeasurements() {
        removeOldMeasurementsService.removeMeasurementsFromSessions()
    }

    private suspend fun delete(uuids: List<String>) {
        sessionRepository.delete(uuids)
    }

    private suspend fun upload(uuids: List<String>) {
        uuids.forEach { uuid ->
            val session = sessionRepository.loadSessionForUploadSuspend(uuid)
            val encodedPhotos = getPhotosFromSessionNotes(uuid)

            session ?: return

            if (isUploadable(session) && encodedPhotos != null) {
                uploadSession(session, encodedPhotos)
            }
        }
    }

    private suspend fun getPhotosFromSessionNotes(uuid: String): List<String?>? {
        val sessionID = sessionRepository.getSessionIdByUUIDSuspend(uuid)
        val mNotes = sessionID?.let { noteRepository.getNotesForSessionWithId(it) }

        val encodedPhotos = mNotes?.filterNotNull()?.map { note ->
            encodeToBase64(note.photo_location?.toUri())
        }
        return encodedPhotos
    }

    private suspend fun uploadSession(session: Session, encodedPhotos: List<String?>) {
        runCatching {
            uploadService.upload(session, encodedPhotos)
        }.onSuccess { response ->
            if (response.isSuccessful) {
                sessionRepository.updateUrlLocation(session, response.body()?.location)
            } else {
                errorHandler.handle(UnexpectedAPIError())
            }
        }.onFailure { exception ->
            errorHandler.handle(UnexpectedAPIError(exception))
        }
    }

    private suspend fun download(uuids: List<String>) {
        uuids.forEach { uuid ->
            if (syncJob?.isCancelled != true) {
                MainScope().launch {
                    downloadService.download(uuid)
                        .onFailure {
                            errorHandler.handle(
                                UnexpectedAPIError(it)
                            )
                        }
                        .onSuccess { session ->
                            if (syncJob?.isCancelled != true) {
                                try {
                                    val sessionId = sessionRepository.updateOrCreate(session)
                                    sessionId?.let {
                                        measurementStreamsRepository.insert(
                                            sessionId,
                                            session.streams
                                        )
                                    }

                                    session.notes.forEach { note ->
                                        sessionId?.let { sessionId ->
                                            noteRepository.insert(
                                                sessionId,
                                                note
                                            )
                                        }
                                    }
                                } catch (e: SQLiteConstraintException) {
                                    errorHandler.handle(DBInsertException(e))
                                }
                            }
                        }
                }
            }
        }

    }

    private fun isUploadable(session: Session): Boolean {
        return !(session.locationless && session.isMobile() || session.isExternal)
    }

    private suspend fun handleSyncError(
        t: Throwable? = null
    ) {
        if (syncJob?.isCancelled != true && !syncInBackground.get()) {
            // TODO: for backward compatibility, remove later
            EventBus.getDefault().post(SessionsSyncErrorEvent(t))

            errorHandler.handle(SyncError(t))
        }

    }

    private fun setSyncStateToFinished() {
        // TODO: for backward compatibility, remove later
        EventBus.getDefault().postSticky(SessionsSyncEvent(false))

        if (syncAfterDeletion.get()) {
            syncAfterDeletion.set(false)
            sync()
        }
    }

    fun resume() {
        syncInBackground.set(false)
        if (triedToSyncBackground.get()) {
            triedToSyncBackground.set(false)
            sync()
        }
    }

    fun pause() {
        syncInBackground.set(true)
    }
}
