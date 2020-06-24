package io.lunarlogic.aircasting.sensor

import android.content.Context
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.events.DeleteSessionEvent
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.events.StartRecordingEvent
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.networking.services.ApiService
import io.lunarlogic.aircasting.networking.services.SyncService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class SessionManager(private val mContext: Context, private val apiService: ApiService) {
    private val sessionSyncService = SyncService(apiService, ErrorHandler(mContext))
    private val sessionsRespository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val measurementsRepository = MeasurementsRepository()

    @Subscribe
    fun onMessageEvent(event: StartRecordingEvent) {
        startRecording(event.session)
    }

    @Subscribe
    fun onMessageEvent(event: StopRecordingEvent) {
        stopRecording(event.sessionUUID)
    }

    @Subscribe
    fun onMessageEvent(event: NewMeasurementEvent) {
        addMeasurement(event)
    }

    @Subscribe
    fun onMessageEvent(event: DeleteSessionEvent) {
        deleteSession(event.sessionUUID)
    }

    fun onStart() {
        registerToEventBus()
        stopMobileSessions()
    }

    fun onStop() {
        unregisterFromEventBus()
    }

    private fun registerToEventBus() {
        EventBus.getDefault().register(this);
    }

    private fun unregisterFromEventBus() {
        EventBus.getDefault().unregister(this);
    }

    private fun stopMobileSessions() {
        DatabaseProvider.runQuery { sessionsRespository.stopMobileSessions() }
    }

    private fun addMeasurement(event: NewMeasurementEvent) {
        val measurementStream = MeasurementStream(event)

        val location = LocationHelper.lastLocation()
        val measurement = Measurement(event, location?.latitude , location?.longitude)

        val deviceId = event.deviceId ?: return

        DatabaseProvider.runQuery {
            val sessionId = sessionsRespository.getActiveSessionIdByDeviceId(deviceId)
            sessionId?.let {
                val measurementStreamId = measurementStreamsRepository.getIdOrInsert(sessionId, measurementStream)
                measurementsRepository.insert(measurementStreamId, measurement)
            }
        }
    }

    private fun startRecording(session: Session) {
        session.startRecording()

        DatabaseProvider.runQuery {
            sessionsRespository.insert(session)
        }
    }

    private fun stopRecording(uuid: String) {
        DatabaseProvider.runQuery {
            val session = sessionsRespository.loadSessionAndMeasurementsByUUID(uuid)
            session?.let {
                it.stopRecording()
                sessionsRespository.update(it)
                sessionSyncService.sync()
            }
        }
    }

    private fun deleteSession(sessionUUID: String) {
        DatabaseProvider.runQuery {
            sessionsRespository.markForRemoval(listOf(sessionUUID))
        }
    }
}