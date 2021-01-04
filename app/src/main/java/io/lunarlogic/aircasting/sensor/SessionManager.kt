package io.lunarlogic.aircasting.sensor

import android.content.Context
import android.widget.Toast
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.events.*
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.networking.services.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class SessionManager(private val mContext: Context, private val apiService: ApiService, settings: Settings) {
    private val errorHandler = ErrorHandler(mContext)
    private val sessionsSyncService = SessionsSyncService.get(apiService, errorHandler, settings)
    private val sessionUpdateService = UpdateSessionService(apiService, errorHandler, mContext)
    private val fixedSessionUploadService = FixedSessionUploadService(apiService, errorHandler)
    private val fixedSessionDownloadMeasurementsService = FixedSessionDownloadMeasurementsService(apiService, errorHandler)
    private val exportSessionService = ExportSessionService(apiService, errorHandler)
    private val sessionsRespository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val measurementsRepository = MeasurementsRepository()

    @Subscribe
    fun onMessageEvent(event: StartRecordingEvent) {
        startRecording(event.session, event.wifiSSID, event.wifiPassword)
    }

    @Subscribe
    fun onMessageEvent(event: StopRecordingEvent) {
        stopRecording(event.sessionUUID)
    }

    @Subscribe
    fun onMessageEvent(event: SensorDisconnectedEvent) {
        disconnectSession(event.deviceId)
    }

    @Subscribe
    fun onMessageEvent(event: NewMeasurementEvent) {
        addMeasurement(event)
    }

    @Subscribe
    fun onMessageEvent(event: EditSessionEvent){
        editSession(event)
    }

    @Subscribe
    fun onMessageEvent(event: ExportSessionEvent){
        exportSession(event)
    }

    @Subscribe
    fun onMessageEvent(event: DeleteSessionEvent) {
        deleteSession(event.sessionUUID)
    }

    fun onStart() {
        registerToEventBus()
        updateMobileSessions()
        fixedSessionDownloadMeasurementsService.start()
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

    private fun updateMobileSessions() {
        DatabaseProvider.runQuery {
            sessionsRespository.disconnectMobileBluetoothSessions()
            sessionsRespository.finishMobileMicSessions()
        }
    }

    private fun addMeasurement(event: NewMeasurementEvent) {
        val measurementStream = MeasurementStream(event)

        val location = LocationHelper.lastLocation()
        val measurement = Measurement(event, location?.latitude , location?.longitude)

        val deviceId = event.deviceId ?: return

        DatabaseProvider.runQuery {
            val sessionId = sessionsRespository.getMobileActiveSessionIdByDeviceId(deviceId)
            sessionId?.let {
                val measurementStreamId = measurementStreamsRepository.getIdOrInsert(sessionId, measurementStream)
                measurementsRepository.insert(measurementStreamId, sessionId, measurement)
            }
        }
    }

    private fun startRecording(session: Session, wifiSSID: String?, wifiPassword: String?) {
        EventBus.getDefault().post(ConfigureSession(session, wifiSSID, wifiPassword))

        session.startRecording()
        if (session.isFixed()) {
            session.follow()
            fixedSessionUploadService.upload(session)
        }

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
                sessionsSyncService.sync()
            }
        }
    }

    private fun disconnectSession(deviceId: String) {
        DatabaseProvider.runQuery {
            sessionsRespository.disconnectSession(deviceId)
        }
    }

    private fun editSession(event: EditSessionEvent) {
        sessionUpdateService.update(event.session) {
            DatabaseProvider.runQuery {
                sessionsRespository.update(event.session)
            }
        }
    }

    private fun exportSession(event: ExportSessionEvent) {
        exportSessionService.export(event.email, event.session.uuid, {
            Toast.makeText(mContext, "Exported sessions will be emailed within minutes. The email may end up in your spam folder.", Toast.LENGTH_LONG).show()
        })
    }

    private fun deleteSession(sessionUUID: String) {
        DatabaseProvider.runQuery {
            sessionsRespository.markForRemoval(listOf(sessionUUID))
            sessionsSyncService.sync()
        }
    }
}
