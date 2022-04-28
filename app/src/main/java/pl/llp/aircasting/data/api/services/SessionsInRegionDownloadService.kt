package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.responses.SessionsInRegionResponse
import pl.llp.aircasting.data.model.Session
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SessionsInRegionDownloadService {
    val sessions: MutableList<Session> = mutableListOf()

    fun add(session: Session) {
        sessions.add(session)
    }
}