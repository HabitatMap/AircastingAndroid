package io.lunarlogic.aircasting.networking.services

import com.google.gson.Gson
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.SyncError
import io.lunarlogic.aircasting.networking.params.SyncSessionBody
import io.lunarlogic.aircasting.networking.params.SyncSessionParams
import io.lunarlogic.aircasting.networking.responses.SyncResponse
import io.lunarlogic.aircasting.sensor.Session
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.atomic.AtomicBoolean

class SyncService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    private val uploadService = UploadService(apiService, errorHandler)
    private val downloadService = DownloadService(apiService, errorHandler)

    private val sessionRepository = SessionsRepository()
    private val gson = Gson()
    private val syncStarted = AtomicBoolean(false)

    fun sync(callback: (() -> Unit)? = null) {
        if (syncStarted.get()) {
            return
        }

        syncStarted.set(true)
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
                    callback?.invoke()

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
                    callback?.invoke()
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
            if (session != null && session.isUploadable()) {
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
                DatabaseProvider.runQuery { sessionRepository.updateOrCreate(session) }
            }
            downloadService.download(uuid, onDownloadSuccess)
        }
    }
}