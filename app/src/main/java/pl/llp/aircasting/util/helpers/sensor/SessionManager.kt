package pl.llp.aircasting.util.helpers.sensor

import android.content.Context
import android.widget.Toast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.*
import pl.llp.aircasting.data.local.repository.*
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.*
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.runOnIOThread
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.extensions.showToast
import pl.llp.aircasting.util.helpers.sensor.microphone.MicrophoneDeviceItem
import pl.llp.aircasting.util.helpers.sensor.new_measurement_handler.NewMeasurementAirBeamHandler
import pl.llp.aircasting.util.helpers.sensor.new_measurement_handler.NewMeasurementSingleStreamHandler
import pl.llp.aircasting.util.helpers.services.AveragingBackgroundService
import pl.llp.aircasting.util.helpers.services.AveragingPreviousMeasurementsBackgroundService
import pl.llp.aircasting.util.helpers.services.AveragingService

class SessionManager(
    private val mContext: Context,
    apiService: ApiService,
    private val settings: Settings,
    private val errorHandler: ErrorHandler = ErrorHandler(mContext),
    private val sessionsRepository: SessionsRepository = SessionsRepository(),
    private val measurementStreamsRepository: MeasurementStreamsRepository = MeasurementStreamsRepository(),
    private val measurementsRepository: MeasurementsRepository = MeasurementsRepository(),
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository = ActiveSessionMeasurementsRepository(),
    private val noteRepository: NoteRepository = NoteRepository(),
    private val newMeasurementAirBeamHandler: NewMeasurementAirBeamHandler = NewMeasurementAirBeamHandler(
        settings,
        errorHandler,
        sessionsRepository,
        measurementStreamsRepository,
        measurementsRepository,
        activeSessionMeasurementsRepository
    ),
    private val newMeasurementMicrophoneHandler: NewMeasurementSingleStreamHandler = NewMeasurementSingleStreamHandler(
        settings,
        errorHandler,
        sessionsRepository,
        measurementStreamsRepository,
        measurementsRepository,
        activeSessionMeasurementsRepository
    )
) {
    private val sessionsSyncService = SessionsSyncService.get(apiService, errorHandler, settings)
    private val sessionUpdateService = UpdateSessionService(apiService, errorHandler, mContext)
    private val exportSessionService = ExportSessionService(apiService, errorHandler, mContext)
    private val fixedSessionUploadService = FixedSessionUploadService(apiService, errorHandler)
    private val fixedSessionDownloadMeasurementsService =
        PeriodicallyDownloadFixedSessionMeasurementsService(apiService, errorHandler)
    private val periodicallySyncSessionsService =
        PeriodicallySyncSessionsService(settings, sessionsSyncService)
    private var averagingBackgroundService: AveragingBackgroundService? = null
    private var averagingPreviousMeasurementsBackgroundService: AveragingPreviousMeasurementsBackgroundService? =
        null
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
    fun onMessageEvent(event: StandaloneModeEvent) {
        startStandaloneMode(event.sessionUUID)
    }

    @Subscribe
    fun onMessageEvent(event: SensorDisconnectedEvent) {
        disconnectSession(event.sessionDeviceId)
    }

    @Subscribe
    fun onMessageEvent(event: NoteCreatedEvent) {
        addNote(event)
    }

    @Subscribe
    fun onMessageEvent(event: NoteEditedEvent) {
        editNote(event)
    }

    @Subscribe
    fun onMessageEvent(event: NoteDeletedEvent) {
        deleteNote(event)
    }

    // ASYNC handles all addMeasurement() in a different thread (potentially creating a lot of them)
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: NewMeasurementEvent) = when (event.sensorPackageName) {
        MicrophoneDeviceItem.DEFAULT_ID -> newMeasurementMicrophoneHandler.handle(event)
        else -> newMeasurementAirBeamHandler.handle(event)
    }

    @Subscribe
    fun onMessageEvent(event: UpdateSessionEvent) {
        updateSession(event)
    }

    @Subscribe
    fun onMessageEvent(event: ExportSessionEvent) {
        exportSession(event)
    }

    @Subscribe
    fun onMessageEvent(event: DeleteSessionEvent) {
        deleteSession(event.sessionUUID)
    }

    @Subscribe
    fun onMessageEvent(event: LogoutEvent) {
        fixedSessionDownloadMeasurementsService.stop()
        periodicallySyncSessionsService.stop()
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

        // we only want to do this after a crash/restart becuase MainActivity can be destroyed when the app is in the background
        // https://stackoverflow.com/questions/59648644/foreground-service-content-intent-not-resuming-the-app-but-relaunching-it
        if (settings.appRestarted()) {
            updateMobileSessions()
            settings.setAppNotRestarted()
        }
        periodicallySyncSessionsService.start()
        fixedSessionDownloadMeasurementsService.start()
    }

    fun onStop() {
        unregisterFromEventBus()
    }

    private fun onAppToForeground() {
        fixedSessionDownloadMeasurementsService.resume()
        sessionsSyncService.resume()
        periodicallySyncSessionsService.resume()
    }

    private fun onAppToBackground() {
        fixedSessionDownloadMeasurementsService.pause()
        sessionsSyncService.pause()
        periodicallySyncSessionsService.pause()
    }

    private fun registerToEventBus() {
        EventBus.getDefault().safeRegister(this)
    }

    private fun unregisterFromEventBus() {
        EventBus.getDefault().unregister(this)
    }

    private fun updateMobileSessions() {
        runOnIOThread {
            sessionsRepository.disconnectMobileBluetoothSessions()
            sessionsRepository.finishMobileMicSessions()
        }
    }

    private fun startRecording(session: Session, wifiSSID: String?, wifiPassword: String?) {
        var DBsessionId: Long? = null

        EventBus.getDefault().post(ConfigureSession(session, wifiSSID, wifiPassword))

        session.startRecording()

        if (session.isFixed()) {
            session.setFollowedAtNow()
            settings.increaseFollowedSessionsCount()
            fixedSessionUploadService.upload(session)
        }

        runOnIOThread {
            DBsessionId = sessionsRepository.insert(session)
            if (session.isMobile()) {
                DBsessionId?.let {
                    val averagingService = AveragingService.get(it)

                    averagingService?.let { averagingService ->
                        averagingBackgroundService = AveragingBackgroundService(averagingService)
                        averagingBackgroundService?.start()
                        averagingPreviousMeasurementsBackgroundService =
                            AveragingPreviousMeasurementsBackgroundService(averagingService)
                        averagingPreviousMeasurementsBackgroundService?.start()
                    }
                }
            }

        }
    }

    private fun stopRecording(uuid: String) {
        runOnIOThread {
            val sessionId = sessionsRepository.getSessionIdByUUID(uuid)
            val session = sessionsRepository.loadSessionAndMeasurementsByUUID(uuid)
            session?.let {
                it.stopRecording()

                sessionsRepository.update(it)
                activeSessionMeasurementsRepository.deleteBySessionId(sessionId)
                sessionsSyncService.sync()
                averagingBackgroundService?.stop()
                averagingPreviousMeasurementsBackgroundService?.stop()
                AveragingService.destroy(sessionId)
            }
        }
    }

    private fun startStandaloneMode(uuid: String) {
        runOnIOThread {
            val sessionId = sessionsRepository.getSessionIdByUUID(uuid)
            averagingBackgroundService?.stop()
            averagingPreviousMeasurementsBackgroundService?.stop()
            AveragingService.destroy(sessionId)
        }
    }

    private fun disconnectSession(deviceId: String) {
        runOnIOThread {
            sessionsRepository.disconnectSession(deviceId)
        }
    }

    private fun updateSession(event: UpdateSessionEvent) {
        val session = event.session.copy()
        session.name = event.name
        session.tags = event.tags
        sessionUpdateService.update(session) {
            runOnIOThread {
                sessionsRepository.update(session)
            }
        }
    }

    private fun exportSession(event: ExportSessionEvent) {
        exportSessionService.export(event.email, event.session.uuid) {
            mContext.apply {
                showToast(
                    getString(R.string.exported_session_service_success),
                    Toast.LENGTH_LONG
                )
            }
        }
    }

    private fun deleteSession(sessionUUID: String) {
        runOnIOThread {
            settings.setDeletingSessionsInProgress(true)
            sessionsRepository.markForRemoval(listOf(sessionUUID))
            settings.setSessionsToRemove(true)
            settings.setDeletingSessionsInProgress(false)
        }
    }

    private fun deleteStreams(session: Session, streamsToDelete: List<MeasurementStream>?) {
        markForRemoval(session, streamsToDelete) {
            updateSession(session)
        }
    }

    private fun markForRemoval(
        session: Session,
        streamsToDelete: List<MeasurementStream>?,
        callback: () -> Unit
    ) {
        mCallback = callback
        runOnIOThread {
            val sessionId = sessionsRepository.getSessionIdByUUID(session.uuid)
            measurementStreamsRepository.markForRemoval(sessionId, streamsToDelete)
            mCallback?.invoke()
        }
    }

    private fun updateSession(session: Session) {
        val reloadedSession = sessionsRepository.loadSessionForUpload(session.uuid)

        if (reloadedSession != null) {
            sessionUpdateService.update(reloadedSession) {
                deleteMarkedForRemoval()
            }
        }
    }

    private fun deleteMarkedForRemoval() {
        runOnIOThread {
            measurementStreamsRepository.deleteMarkedForRemoval()
            sessionsSyncService.sync()
        }
    }

    private fun addNote(event: NoteCreatedEvent) {
        runOnIOThread {
            val sessionId = sessionsRepository.getSessionIdByUUID(event.session.uuid)
            sessionId?.let {
                noteRepository.insert(sessionId, event.note)
            }
        }
    }

    private fun editNote(event: NoteEditedEvent) {
        runOnIOThread {
            event.session?.let {
                val sessionId = sessionsRepository.getSessionIdByUUID(event.session.uuid)
                if (sessionId != null && event.note != null) {
                    noteRepository.update(sessionId, event.note)
                }
            }
            if (event.session?.endTime != null) event.session.let { session -> updateSession(session) }
        }
    }

    private fun deleteNote(event: NoteDeletedEvent) {
        runOnIOThread {
            event.session?.let {
                val sessionId = sessionsRepository.getSessionIdByUUID(event.session.uuid)
                if (sessionId != null && event.note != null) {
                    noteRepository.delete(sessionId, event.note)
                }
            }
            if (event.session?.endTime != null) event.session.let { session ->
                updateSession(session)
            }
        }
    }
}
