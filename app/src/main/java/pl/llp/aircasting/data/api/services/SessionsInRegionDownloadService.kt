package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.model.Session

class SessionsInRegionDownloadService {
    val sessions: MutableList<Session> = mutableListOf()

    fun add(session: Session) {
        sessions.add(session)
    }
}