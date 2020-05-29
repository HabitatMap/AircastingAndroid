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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SyncService(settings: Settings, private val errorHandler: ErrorHandler) {
    private val uploadService = UploadService(settings, errorHandler)

    private val sessionRepository = SessionsRepository()
    private val apiService =
        ApiServiceFactory.get(settings.getAuthToken()!!)
    private val gson = Gson()

    fun sync() {
        val sessions = sessionRepository.finishedSessions()
        val syncParams = sessions.map { session -> SyncSessionParams(session) }
        val jsonData = gson.toJson(syncParams)
        val call = apiService.sync(SyncSessionBody(jsonData))

        call.enqueue(object : Callback<SyncResponse> {
            override fun onResponse(call: Call<SyncResponse>, response: Response<SyncResponse>) {
                println("ANIA: " + response.message())

                if (response.isSuccessful) {
                    val body = response.body()
                    body?.let {
                        DatabaseProvider.runQuery {
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

    private fun delete(uuids: List<String>) {
        sessionRepository.delete(uuids)
    }

    private fun upload(uuids: List<String>) {
        uuids.forEach { uuid ->
            val session = sessionRepository.loadSessionAndMeasurementsByUUID(uuid)
            if (session != null && session.isUploadable()) {
                uploadService.upload(session)
            }
        }
    }

    private fun download(uuids: List<String>) {
        // TODO: handle
    }
}