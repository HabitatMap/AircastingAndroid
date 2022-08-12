package pl.llp.aircasting.ui.view.screens.dashboard.helpers

import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.Settings
import javax.inject.Inject

class SessionFollower @Inject constructor(
    private val mSettings: Settings,
    private val mActiveSessionsRepository: ActiveSessionMeasurementsRepository,
    private val mSessionRepository: SessionsRepository
) {
    fun follow(session: Session) {
        updateFollowedAt(session)

        addFollowedSessionMeasurementsToActiveTable(session)
        mSettings.increaseFollowedSessionsNumber()
    }

    fun unfollow(session: Session) {
        if (session.isExternal) delete(session)
        else {
            updateFollowedAt(session)
            clearUnfollowedSessionMeasurementsFromActiveTable(session)
        }
        mSettings.decreaseFollowedSessionsNumber()
    }

    private fun updateFollowedAt(session: Session) {
        DatabaseProvider.runQuery {
            mSessionRepository.updateFollowedAt(session)
            mSessionRepository.updateOrder(session.uuid, mSettings.getFollowedSessionsNumber())
        }
    }

    private fun addFollowedSessionMeasurementsToActiveTable(session: Session) {
        DatabaseProvider.runQuery {
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
        DatabaseProvider.runQuery {
            mSessionRepository.delete(session.uuid)
        }
    }

    private fun clearUnfollowedSessionMeasurementsFromActiveTable(session: Session) {
        DatabaseProvider.runQuery {
            val sessionId = mSessionRepository.getSessionIdByUUID(session.uuid)
            mActiveSessionsRepository.deleteBySessionId(sessionId)
        }
    }
}

