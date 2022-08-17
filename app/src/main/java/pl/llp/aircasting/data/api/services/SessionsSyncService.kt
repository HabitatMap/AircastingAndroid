package pl.llp.aircasting.data.api.services

import android.database.sqlite.SQLiteConstraintException
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.data.api.params.SyncSessionBody
import pl.llp.aircasting.data.api.params.SyncSessionParams
import pl.llp.aircasting.data.api.response.SyncResponse
import pl.llp.aircasting.data.api.response.UploadSessionResponse
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.NoteRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.sessions_sync.SessionsSyncErrorEvent
import pl.llp.aircasting.util.events.sessions_sync.SessionsSyncSuccessEvent
import pl.llp.aircasting.util.exceptions.DBInsertException
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SyncError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.atomic.AtomicBoolean

class SessionsSyncService private constructor(
    private val apiService: ApiService,
    private val errorHandler: ErrorHandler,
    private val settings: Settings
) {

    private val uploadService: MobileSessionUploadService =
        MobileSessionUploadService(apiService, errorHandler)
    private val downloadService: SessionDownloadService =
        SessionDownloadService(apiService, errorHandler)
    private val removeOldMeasurementsService: RemoveOldMeasurementsService =
        RemoveOldMeasurementsService()

    private val sessionRepository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val noteRepository = NoteRepository()
    private val gson = Gson()
    private val syncStarted = AtomicBoolean(false)
    private var syncInBackground = AtomicBoolean(false)
    private var triedToSyncBackground = AtomicBoolean(false)
    private var mCall: Call<SyncResponse>? = null

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

    fun destroy() {
        mCall?.cancel()
    }

    fun sync(
        onStartCallback: (() -> Unit)? = null,
        finallyCallback: (() -> Unit)? = null,
        shouldDisplayErrors: Boolean = true
    ) {

        // This will happen if we regain connectivity when app is in background.
        // When in foreground again, it should sync
        if (syncInBackground.get()) {
            triedToSyncBackground.set(true)
        }
        if (syncStarted.get() || syncInBackground.get() || settings.getIsDeleteSessionInProgress()) {
            return
        }

        syncStarted.set(true)
        onStartCallback?.invoke()

        DatabaseProvider.runQuery {
            val sessions = sessionRepository.allSessionsExceptRecording()
            val syncParams = sessions.map { session -> SyncSessionParams(session) }
            val jsonData = gson.toJson(syncParams)

            mCall = apiService.sync(SyncSessionBody(jsonData))
            mCall?.enqueue(object : Callback<SyncResponse> {
                override fun onResponse(
                    call: Call<SyncResponse>,
                    response: Response<SyncResponse>
                ) {

                    if (response.isSuccessful) {
                        val body = response.body()
                        body?.let {
                            DatabaseProvider.runQuery {
                                deleteMarkedForRemoval()
                                delete(body.deleted)
                                removeOldMeasurements()

                                upload(body.upload)
                                download(body.download)
                                EventBus.getDefault().post(SessionsSyncSuccessEvent())
                            }
                        }
                    } else handleSyncError(shouldDisplayErrors, call)

                    syncStarted.set(false)
                    finallyCallback?.invoke()
                }

                override fun onFailure(call: Call<SyncResponse>, t: Throwable) {
                    syncStarted.set(false)
                    finallyCallback?.invoke()
                    handleSyncError(shouldDisplayErrors, call, t)
                }
            })
        }
    }

    private fun removeOldMeasurements() {
        removeOldMeasurementsService.removeMeasurementsFromSessions()
    }

    private fun deleteMarkedForRemoval() {
        sessionRepository.deleteMarkedForRemoval()
    }

    private fun delete(uuids: List<String>) {
        sessionRepository.delete(uuids)
    }

    private fun upload(uuids: List<String>) {
        uuids.forEach { uuid ->
            val session = sessionRepository.loadSessionForUpload(uuid)
            if (session != null && isUploadable(session)) {
                val onUploadSuccess = { response: Response<UploadSessionResponse> ->
                    DatabaseProvider.runQuery {
                        sessionRepository.updateUrlLocation(session, response.body()?.location)
                    }
                    // TODO: handle update notes - adding photoPath
                }
                uploadService.upload(session, onUploadSuccess)
            }
        }
    }

    private fun download(uuids: List<String>) {
        uuids.forEach { uuid ->
            val onDownloadSuccess = { session: Session ->
                DatabaseProvider.runQuery {
                    if (mCall?.isCanceled != true) {
                        try {
                            val sessionId = sessionRepository.updateOrCreate(session)
                            sessionId?.let {
                                measurementStreamsRepository.insert(
                                    sessionId,
                                    session.streams
                                )
                            }
                            for (note in session.notes) {
                                sessionId?.let { sessionId ->
                                    noteRepository.insert(sessionId, note)
                                }
                            }
                        } catch (e: SQLiteConstraintException) {
                            errorHandler.handle(DBInsertException(e))
                        }
                    }
                }
            }
            if (mCall?.isCanceled != true) downloadService.download(uuid, onDownloadSuccess)
        }
    }

    private fun isUploadable(session: Session): Boolean {
        return !(session.locationless && session.isMobile() || session.isExternal)
    }

    private fun handleSyncError(
        shouldDisplayErrors: Boolean,
        call: Call<SyncResponse>,
        t: Throwable? = null
    ) {
        if (!call.isCanceled && !syncInBackground.get()) {
            EventBus.getDefault().post(SessionsSyncErrorEvent())

            if (shouldDisplayErrors) errorHandler.handleAndDisplay(SyncError(t)) else errorHandler.handle(
                SyncError(t)
            )
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
