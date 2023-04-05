package pl.llp.aircasting.util.helpers.sensor

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.*
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.NoteRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.*
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.extensions.showToast
import pl.llp.aircasting.util.helpers.sensor.handlers.RecordingHandler
import javax.inject.Inject

@UserSessionScope
class SessionManager @Inject constructor(
    private val mContext: Context,
    private val settings: Settings,
    private val sessionsRepository: SessionsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val noteRepository: NoteRepository,
    private val sessionsSyncService: SessionsSyncService,
    private val sessionUpdateService: UpdateSessionService,
    private val exportSessionService: ExportSessionService,
    private val fixedSessionDownloadMeasurementsService: PeriodicallyDownloadFixedSessionMeasurementsService,
    private val downloadFollowedSessionMeasurementsService: DownloadFollowedSessionMeasurementsService,
    private val recordingHandler: RecordingHandler,
    @IoCoroutineScope private val coroutineScope: CoroutineScope,
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
        coroutineScope.launch {
            if (settings.appRestarted()) {
                updateMobileSessions()
                settings.setAppNotRestarted()
            }
            fixedSessionDownloadMeasurementsService.start()
        }
    }

    fun onStop() {
        unregisterFromEventBus()
    }

    private fun onAppToForeground() {
        fixedSessionDownloadMeasurementsService.resume()
        coroutineScope.launch {
            downloadFollowedSessionMeasurementsService.downloadMeasurements()
        }
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

    private suspend fun updateMobileSessions() {
        sessionsRepository.disconnectMobileBluetoothSessions()
        sessionsRepository.finishMobileMicSessions()
    }

    private fun updateSession(event: UpdateSessionEvent) {
        coroutineScope.launch {
            val session = event.session.copy()
            session.name = event.name
            session.tags = event.tags
            sessionUpdateService.update(session)
                .onSuccess {
                    sessionsRepository.update(session)
                }
        }
    }

    private fun exportSession(event: ExportSessionEvent) = coroutineScope.launch {
        exportSessionService.export(event.email, event.session.uuid)
            .onSuccess {
                withContext(Dispatchers.Main) {
                    mContext.apply {
                        showToast(
                            getString(R.string.exported_session_service_success),
                            Toast.LENGTH_LONG
                        )
                    }
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
        coroutineScope.launch {
            markForRemoval(session, streamsToDelete)
            updateSession(session)
        }
    }

    private suspend fun markForRemoval(
        session: Session,
        streamsToDelete: List<MeasurementStream>?
    ) {
        val sessionId = sessionsRepository.getSessionIdByUUID(session.uuid)
        measurementStreamsRepository.markForRemoval(sessionId, streamsToDelete)
    }

    private suspend fun updateSession(session: Session) {
        val reloadedSession = sessionsRepository.loadSessionForUpload(session.uuid)

        if (reloadedSession != null) {
            sessionUpdateService.update(reloadedSession)
                .onSuccess {
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
            val sessionId = sessionsRepository.getSessionIdByUUID(event.session.uuid)
            sessionId?.let {
                noteRepository.insert(sessionId, event.note)
            }
        }
    }

    private fun editNote(event: NoteEditedEvent) {
        coroutineScope.launch {
            event.session?.let {
                val sessionId = sessionsRepository.getSessionIdByUUID(event.session.uuid)
                if (sessionId != null && event.note != null) {
                    noteRepository.update(sessionId, event.note)
                }
            }
            if (event.session?.endTime != null)
                updateSession(event.session)
        }
    }

    private fun deleteNote(event: NoteDeletedEvent) {
        coroutineScope.launch {
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
