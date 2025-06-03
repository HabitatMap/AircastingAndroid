package pl.llp.aircasting.util.helpers.sensor.common

import android.util.Log
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.Settings
import java.util.Date
import javax.inject.Inject

fun interface SessionFinisher {
    suspend operator fun invoke(uuid: String)
}

class DefaultSessionFinisher @Inject constructor(
    private val sessionsRepository: SessionsRepository,
    private val settings: Settings,
    private val measurementRepository: MeasurementsRepository,
) : SessionFinisher {
    override suspend fun invoke(uuid: String) {
        sessionsRepository.getSessionByUUID(uuid)
            ?.takeIf { it.isNotFinished }
            .also {
                if (it == null)
                    Log.w(TAG, "Tried to finish $uuid session which is already finished!")
            }
            ?.apply {
                copy(
                    status = Session.Status.FINISHED,
                    endTime = measurementRepository.lastMeasurementTime(id) ?: Date()
                ).let { updatedSession ->
                    sessionsRepository.update(updatedSession)
                    settings.decreaseActiveMobileSessionsCount()
                }
            }
    }
}