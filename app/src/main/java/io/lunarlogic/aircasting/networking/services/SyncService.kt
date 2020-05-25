package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.params.SyncSessionBody
import io.lunarlogic.aircasting.networking.params.SyncSessionParams
import io.lunarlogic.aircasting.networking.responses.SyncResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SyncService(settings: Settings) {
    private val sessionRepository = SessionsRepository()
    private val apiService =
        ApiServiceFactory.get(settings.getAuthToken()!!)

    fun sync() {
        val sessions = sessionRepository.finishedSessions()
        val syncParams = sessions.map { session -> SyncSessionParams(session) }
        val call = apiService.sync(SyncSessionBody(syncParams))

        call.enqueue(object : Callback<SyncResponse> {
            override fun onResponse(call: Call<SyncResponse>, response: Response<SyncResponse>) {
                println("ANIA: " + response.message())
            }

            override fun onFailure(call: Call<SyncResponse>, t: Throwable) {
                println("ANIA: error :(")
            }
        })

//        val status = result.getStatus()
//        if (status === Status.ERROR || status === Status.FAILURE) {
//            throw SessionSyncException("Initial sync failed")
//        }
//        val syncResponse = result.getContent()
//
//        if (syncResponse != null) {
//            sessionRepository.deleteSubmitted()
//            val upload = syncResponse!!.getUpload()
//            val deleted = syncResponse!!.getDeleted()
//            val download = syncResponse!!.getDownload()
//            deleteMarked(deleted)
//            uploadSessions(upload)
//            downloadSessions(download)
//        }
    }
}