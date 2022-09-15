package pl.llp.aircasting.ui.view.screens.dashboard.helpers

import kotlinx.coroutines.*
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.modules.IoDispatcher
import pl.llp.aircasting.util.Settings
import javax.inject.Inject

class SessionFollower @Inject constructor(
    private val mSettings: Settings,
    private val mActiveSessionsRepository: ActiveSessionMeasurementsRepository,
    private val mSessionRepository: SessionsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun follow(session: Session) {
        session.setFollowedAtNow()
        updateFollowedAt(session)

        addFollowedSessionMeasurementsToActiveTable(session)
        mSettings.increaseFollowedSessionsNumber()
    }

    fun unfollow(session: Session) {
        if (session.isExternal) delete(session)
        else {
            session.resetFollowedAtAndOrder()
            updateFollowedAt(session)
            clearUnfollowedSessionMeasurementsFromActiveTable(session)
        }
        mSettings.decreaseFollowedSessionsNumber()
    }

    private fun updateFollowedAt(session: Session) {
        CoroutineScope(ioDispatcher).launch {
            mSessionRepository.updateFollowedAt(session)
            mSessionRepository.updateOrder(session.uuid, mSettings.followedSessionsCount())
        }
    }

    private fun addFollowedSessionMeasurementsToActiveTable(session: Session) {
        CoroutineScope(ioDispatcher).launch {
            val sessionId = mSessionRepository.getSessionIdByUUID(session.uuid)
            sessionId?.let {
                mActiveSessionsRepository.loadMeasurementsForStreams(
                    it,
                    session.streams,
                    ActiveSessionMeasurementsRepository.MAX_MEASUREMENTS_PER_STREAM_NUMBER
                )
            }
        }
    }

    private fun delete(session: Session) {
        CoroutineScope(ioDispatcher).launch {
            mSessionRepository.delete(session.uuid)
        }
    }

    private fun clearUnfollowedSessionMeasurementsFromActiveTable(session: Session) {
        CoroutineScope(ioDispatcher).launch {
            val sessionId = mSessionRepository.getSessionIdByUUID(session.uuid)
            mActiveSessionsRepository.deleteBySessionId(sessionId)
        }
    }
}

