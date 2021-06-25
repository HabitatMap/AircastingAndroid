package pl.llp.aircasting.networking.services

import android.database.sqlite.SQLiteConstraintException
import com.google.gson.Gson
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.events.sessions_sync.SessionsSyncErrorEvent
import pl.llp.aircasting.events.sessions_sync.SessionsSyncSuccessEvent
import pl.llp.aircasting.exceptions.DBInsertException
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.SyncError
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.networking.params.SyncSessionBody
import pl.llp.aircasting.networking.params.SyncSessionParams
import pl.llp.aircasting.networking.responses.SyncResponse
import pl.llp.aircasting.networking.responses.UploadSessionResponse
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.atomic.AtomicBoolean

class SessionsSyncService {
    private val apiService: ApiService
    private val errorHandler: ErrorHandler
    private val settings: Settings

    private val uploadService: MobileSessionUploadService
    private val downloadService: SessionDownloadService
    private val removeOldMeasurementsService: RemoveOldMeasurementsService

    private val sessionRepository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val gson = Gson()
    private val syncStarted = AtomicBoolean(false)
    private var syncInBackground = AtomicBoolean(false)
    private var triedToSyncBackground = AtomicBoolean(false)
    private var mCall: Call<SyncResponse>? = null


    private constructor(apiService: ApiService, errorHandler: ErrorHandler, settings: Settings) {
        this.apiService = apiService
        this.errorHandler = errorHandler
        this.settings = settings

        this.uploadService = MobileSessionUploadService(apiService, errorHandler)
        this.downloadService = SessionDownloadService(apiService, errorHandler)
        this.removeOldMeasurementsService = RemoveOldMeasurementsService()
    }

    companion object {
        private var mSingleton: SessionsSyncService? = null

        fun get(apiService: ApiService, errorHandler: ErrorHandler, settings: Settings): SessionsSyncService {
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
        if (syncStarted.get() || syncInBackground.get() || settings.getIsDeleteSessionInProgress() == true) {
            return
        }

        syncStarted.set(true)
        onStartCallback?.invoke()

        DatabaseProvider.runQuery {
            val sessions = sessionRepository.finishedSessions()
            val syncParams = sessions.map { session -> SyncSessionParams(session) }
            val jsonData = gson.toJson(syncParams)
            mCall = apiService.sync(SyncSessionBody(jsonData))

            mCall?.enqueue(object : Callback<SyncResponse> {
                override fun onResponse(
                    call: Call<SyncResponse>,
                    response: Response<SyncResponse>
                ) {
                    syncStarted.set(false)
                    finallyCallback?.invoke()

                    if (response.isSuccessful) {
                        val body = response.body()
                        body?.let {
                            DatabaseProvider.runQuery {
                                deleteMarkedForRemoval()
                                delete(body.deleted)
                                upload(body.upload)
                                download(body.download)
                                removeOldMeasurements()

                                EventBus.getDefault().post(SessionsSyncSuccessEvent())
                            }
                        }
                    } else {
                        handleSyncError(shouldDisplayErrors, call)
                    }
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
                        } catch (e: SQLiteConstraintException) {
                            errorHandler.handle(DBInsertException(e))
                        }
                    }
                }
            }
            if (mCall?.isCanceled != true) {
                downloadService.download(uuid, onDownloadSuccess)
            }
        }
    }

    private fun isUploadable(session: Session): Boolean {
        return !(session.locationless && session.isMobile())
    }

    private fun handleSyncError(shouldDisplayErrors: Boolean, call: Call<SyncResponse>, t: Throwable? = null) {
        if (!call.isCanceled && !syncInBackground.get()) {
            EventBus.getDefault().post(SessionsSyncErrorEvent())

            if (shouldDisplayErrors) {
                errorHandler.handleAndDisplay(SyncError(t))
            } else {
                errorHandler.handle(SyncError(t))
            }
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
