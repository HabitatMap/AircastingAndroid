package io.lunarlogic.aircasting.networking.services

import com.google.gson.Gson
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.SyncError
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.params.SyncSessionBody
import io.lunarlogic.aircasting.networking.params.SyncSessionParams
import io.lunarlogic.aircasting.networking.responses.SyncResponse
import io.lunarlogic.aircasting.models.Session
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

    private val sessionRepository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val gson = Gson()
    private val syncStarted = AtomicBoolean(false)

    private constructor(apiService: ApiService, errorHandler: ErrorHandler, settings: Settings) {
        this.apiService = apiService
        this.errorHandler = errorHandler
        this.settings = settings

        this.uploadService = MobileSessionUploadService(apiService, errorHandler)
        this.downloadService = SessionDownloadService(apiService, errorHandler)
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
            mSingleton = null
        }
    }

    fun sync(showLoaderCallback: (() -> Unit)? = null, hideLoaderCallback: (() -> Unit)? = null) {
        if (syncStarted.get()) {
            return
        }

        syncStarted.set(true)
        showLoaderCallback?.invoke()

        DatabaseProvider.runQuery {
            val sessions = sessionRepository.finishedSessions()
            val syncParams = sessions.map { session -> SyncSessionParams(session) }
            val jsonData = gson.toJson(syncParams)
            val call = apiService.sync(SyncSessionBody(jsonData))

            call.enqueue(object : Callback<SyncResponse> {
                override fun onResponse(
                    call: Call<SyncResponse>,
                    response: Response<SyncResponse>
                ) {
                    syncStarted.set(false)
                    hideLoaderCallback?.invoke()

                    if (response.isSuccessful) {
                        val body = response.body()
                        body?.let {
                            DatabaseProvider.runQuery {
                                deleteMarkedForRemoval()
                                delete(body.deleted)
                                upload(body.upload)
                                download(body.download)
                            }
                        }
                    } else {
                        errorHandler.handleAndDisplay(SyncError())
                    }
                }

                override fun onFailure(call: Call<SyncResponse>, t: Throwable) {
                    syncStarted.set(false)
                    hideLoaderCallback?.invoke()
                    errorHandler.handleAndDisplay(SyncError(t))
                }
            })
        }
    }

    private fun deleteMarkedForRemoval() {
        sessionRepository.deleteMarkedForRemoval()
    }

    private fun delete(uuids: List<String>) {
        sessionRepository.delete(uuids)
    }

    private fun upload(uuids: List<String>) {
        uuids.forEach { uuid ->
            val session = sessionRepository.loadSessionAndMeasurementsByUUID(uuid)
            if (session != null && isUploadable(session)) {
                val onUploadSuccess = {
                    // TODO: handle update notes etc
                }
                uploadService.upload(session, onUploadSuccess)
            }
        }
    }

    private fun download(uuids: List<String>) {
        uuids.forEach { uuid ->
            val onDownloadSuccess = { session: Session ->
                DatabaseProvider.runQuery {
                    val sessionId = sessionRepository.updateOrCreate(session)
                    sessionId?.let { measurementStreamsRepository.insert(sessionId, session.streams) }
                }
            }
            downloadService.download(uuid, onDownloadSuccess)

            // WIP
//            val onDownloadSuccess = { session: Session ->
//                DatabaseProvider.runQuery {
//                    sessionRepository.update(session)
//                }
//            }
//            downloadService.download(uuid, onDownloadSuccess)
        }
    }

    private fun isUploadable(session: Session): Boolean {
        return !session.locationless && session.isMobile()
    }
}
