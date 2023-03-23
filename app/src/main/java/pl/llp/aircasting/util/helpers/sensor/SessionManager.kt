package pl.llp.aircasting.util.helpers.sensor

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
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
import pl.llp.aircasting.util.helpers.sensor.handlers.RecordingHandler
import pl.llp.aircasting.util.helpers.sensor.handlers.RecordingHandlerImpl
import pl.llp.aircasting.util.helpers.services.AveragingService
import pl.llp.aircasting.util.helpers.services.MeasurementsAveragingHelperDefault

class SessionManager(
    private val mContext: Context,
    apiService: ApiService,
    private val settings: Settings,
    private val errorHandler: ErrorHandler = ErrorHandler(mContext),
    private val sessionsRepository: SessionsRepository = SessionsRepository(),
    private val measurementStreamsRepository: MeasurementStreamsRepository
    = MeasurementStreamsRepository(),
    private val measurementsRepository: MeasurementsRepositoryImpl = MeasurementsRepositoryImpl(),
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository
    = ActiveSessionMeasurementsRepository(),
    private val noteRepository: NoteRepository = NoteRepository(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val sessionsSyncService: SessionsSyncService
    = SessionsSyncService.get(apiService, errorHandler),
    private val sessionUpdateService: UpdateSessionService
    = UpdateSessionService(apiService, errorHandler, mContext),
    private val exportSessionService: ExportSessionService
    = ExportSessionService(apiService, errorHandler, mContext),
    private val fixedSessionUploadService: FixedSessionUploadService
    = FixedSessionUploadService(apiService, errorHandler),
    private val fixedSessionDownloadMeasurementsService: PeriodicallyDownloadFixedSessionMeasurementsService
    = PeriodicallyDownloadFixedSessionMeasurementsService(apiService, errorHandler),
    private var mCallback: (() -> Unit)? = null,

    private val recordingHandler: RecordingHandler = RecordingHandlerImpl(
        coroutineScope,
        settings,
        fixedSessionUploadService,
        sessionsRepository,
        activeSessionMeasurementsRepository,
        sessionsSyncService,
        errorHandler,
        measurementStreamsRepository,
        measurementsRepository,
        averagingService = AveragingService(
            measurementsRepository,
            measurementStreamsRepository,
            sessionsRepository,
            MeasurementsAveragingHelperDefault()
        )
    )
) {

    @Subscribe
    fun onMessageEvent(event: StartRecordingEvent) {
        recordingHandler.startRecording(event.session, event.wifiSSID, event.wifiPassword)
    }

    @Subscribe
    fun onMessageEvent(event: StopRecordingEvent) {
        recordingHandler.stopRecording(event.sessionUUID)
    }

    @Subscribe
    fun onMessageEvent(event: StandaloneModeEvent) {
        recordingHandler.startStandaloneMode(event.sessionUUID)
    }

    @Subscribe
    fun onMessageEvent(event: SensorDisconnectedEvent) {
        recordingHandler.disconnectSession(event.sessionDeviceId)
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

    @Subscribe
    fun onMessageEvent(event: NewMeasurementEvent) {
        recordingHandler.handle(event)
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
        fixedSessionDownloadMeasurementsService.start()
    }

    fun onStop() {
        unregisterFromEventBus()
    }

    private fun onAppToForeground() {
        fixedSessionDownloadMeasurementsService.resume()
    }

    private fun onAppToBackground() {
        fixedSessionDownloadMeasurementsService.pause()
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
        coroutineScope.launch {
            sessionsRepository.markForRemoval(sessionUUID)
            sessionsSyncService.sync()
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
        coroutineScope.launch {
            measurementStreamsRepository.deleteMarkedForRemoval()
            sessionsSyncService.sync()
        }
    }

    private fun addNote(event: NoteCreatedEvent) {
        coroutineScope.launch {
            val sessionId = sessionsRepository.getSessionIdByUUIDSuspend(event.session.uuid)
            sessionId?.let {
                noteRepository.insert(sessionId, event.note)
            }
        }
    }

    private fun editNote(event: NoteEditedEvent) {
        coroutineScope.launch {
            event.session?.let {
                val sessionId = sessionsRepository.getSessionIdByUUIDSuspend(event.session.uuid)
                if (sessionId != null && event.note != null) {
                    noteRepository.update(sessionId, event.note)
                }
            }
            if (event.session?.endTime != null)
                updateSession(event.session)
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
            if (event.session?.endTime != null)
                updateSession(event.session)
        }
    }
}
