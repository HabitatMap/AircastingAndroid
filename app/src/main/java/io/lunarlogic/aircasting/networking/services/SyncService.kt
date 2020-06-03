package io.lunarlogic.aircasting.networking.services

import com.google.gson.Gson
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.SyncError
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.params.SyncSessionBody
import io.lunarlogic.aircasting.networking.params.SyncSessionParams
import io.lunarlogic.aircasting.networking.responses.SyncResponse
import io.lunarlogic.aircasting.sensor.Session
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SyncService(settings: Settings, private val errorHandler: ErrorHandler) {
    private val uploadService = UploadService(settings, errorHandler)
    private val downloadService = DownloadService(settings, errorHandler)

    private val sessionRepository = SessionsRepository()
    private val apiService =
        ApiServiceFactory.get(settings.getAuthToken()!!)
    private val gson = Gson()

    fun sync() {
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