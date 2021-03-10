package io.lunarlogic.aircasting.sensor

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.widget.Toast
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.events.*
import io.lunarlogic.aircasting.exceptions.DBInsertException
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.lib.safeRegister
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.networking.services.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SessionManager(private val mContext: Context, private val apiService: ApiService, private val settings: Settings) {
    private val errorHandler = ErrorHandler(mContext)
    private val sessionsSyncService = SessionsSyncService.get(apiService, errorHandler, settings)
    private val sessionUpdateService = UpdateSessionService(apiService, errorHandler, mContext)
    private val exportSessionService = ExportSessionService(apiService, errorHandler, mContext)
    private val fixedSessionUploadService = FixedSessionUploadService(apiService, errorHandler)
    private val fixedSessionDownloadMeasurementsService = PeriodicallyDownloadFixedSessionMeasurementsService(apiService, errorHandler)
    private val sessionsRespository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val measurementsRepository = MeasurementsRepository()
    private var mCallback: (() -> Unit)? = null

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
    fun onMessageEvent(event: NoteCreatedEvent) {
        addNote(event)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: NewMeasurementEvent) {
        addMeasurement(event)
    }

    @Subscribe
    fun onMessageEvent(event: UpdateSessionEvent){
        updateSession(event)
    }

    @Subscribe
    fun onMessageEvent(event: ExportSessionEvent){
        exportSession(event)
    }

    @Subscribe
    fun onMessageEvent(event: DeleteSessionEvent) {
        deleteSession(event.sessionUUID)
    }

    @Subscribe
    fun onMessageEvent(event: LogoutEvent) {
        fixedSessionDownloadMeasurementsService.stop()
        SessionsSyncService.destroy()
    }

    @Subscribe
    fun onMessageEvent(event: AppToForegroundEvent) {
        onAppToForeground()
    }

    @Subscribe
    fun onMessageEvent(event: AppToBackgroundEvent) {
        onAppToBackground()
    }

    @Subscribe
    fun onMessageEvent(event: DeleteStreamsEvent) {
        deleteStreams(event.session, event.streamsToDelete)
    }

    fun onStart() {
        registerToEventBus()
        updateMobileSessions()
        fixedSessionDownloadMeasurementsService.start()
    }

    fun onStop() {
        unregisterFromEventBus()
    }

    fun onAppToForeground() {
        fixedSessionDownloadMeasurementsService.resume()
        sessionsSyncService.resume()
    }

    fun onAppToBackground() {
        fixedSessionDownloadMeasurementsService.pause()
        sessionsSyncService.pause()
    }

    private fun registerToEventBus() {
        EventBus.getDefault().safeRegister(this);
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
                try {
                    val measurementStreamId =
                        measurementStreamsRepository.getIdOrInsert(sessionId, measurementStream)
                    measurementsRepository.insert(measurementStreamId, sessionId, measurement)
                } catch( e: SQLiteConstraintException) {
                    errorHandler.handle(DBInsertException(e))
                }
            }
        }
    }

    private fun startRecording(session: Session, wifiSSID: String?, wifiPassword: String?) {
        EventBus.getDefault().post(ConfigureSession(session, wifiSSID, wifiPassword))

        session.startRecording()
        if (session.isAirBeam3()) {
            settings.setAirbeam3Connected()
        }

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

    private fun updateSession(event: UpdateSessionEvent) {
        val session = event.session.copy()
        session.name = event.name
        session.tags = event.tags
        sessionUpdateService.update(session) {
            DatabaseProvider.runQuery {
                sessionsRespository.update(session)
            }
        }
    }

    private fun exportSession(event: ExportSessionEvent) {
        exportSessionService.export(event.email, event.session.uuid) {
            Toast.makeText(
                mContext,
                mContext.getString(R.string.exported_session_service_success),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun deleteSession(sessionUUID: String) {
        DatabaseProvider.runQuery {
            sessionsRespository.markForRemoval(listOf(sessionUUID))
            sessionsSyncService.sync()
        }
    }

    private fun deleteStreams(session: Session, streamsToDelete: List<MeasurementStream>?) {
        markForRemoval(session, streamsToDelete) {
            updateSession(session)
        }
    }

    private fun markForRemoval(session: Session, streamsToDelete: List<MeasurementStream>?, callback: () -> Unit) {
        mCallback = callback
        DatabaseProvider.runQuery {
            val sessionId = sessionsRespository.getSessionIdByUUID(session.uuid)
            measurementStreamsRepository.markForRemoval(sessionId, streamsToDelete)
            mCallback?.invoke()
        }
    }

    private fun updateSession(session: Session) {
        val reloadedSession = sessionsRespository.loadSessionAndMeasurementsByUUID(session.uuid)
        if (reloadedSession != null) {
            sessionUpdateService.update(reloadedSession) {
                deleteMarkedForRemoval()
            }
        }
    }

    private fun deleteMarkedForRemoval() {
        DatabaseProvider.runQuery {
            measurementStreamsRepository.deleteMarkedForRemoval()
            sessionsSyncService.sync()
        }
    }

    private fun addNote(event: NoteCreatedEvent) {
        // TODO: add reaction to add note pressed by user
        // todo: do i need to add some call methods? handling this in local database
    }
}
