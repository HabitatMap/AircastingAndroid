package pl.llp.aircasting.util.helpers.sensor.common

import android.util.Log
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import javax.inject.Inject

fun interface SessionFinisher {
    suspend operator fun invoke(uuid: String)
}

class DefaultSessionFinisher @Inject constructor(
    private val sessionsRepository: SessionsRepository,
): SessionFinisher {
    override suspend fun invoke(uuid: String) {
        sessionsRepository.getSessionByUUID(uuid)
            ?.takeIf { it.isNotFinished }
            .also { if (it == null)
                Log.w(TAG, "Tried to finish $uuid session which is already finished!")
            }
            ?.let { sessionsRepository.updateSessionStatus(it.uuid, Session.Status.FINISHED) }
    }
}