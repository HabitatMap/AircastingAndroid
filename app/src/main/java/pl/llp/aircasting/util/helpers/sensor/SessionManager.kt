package pl.llp.aircasting.util.helpers.sensor

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.widget.Toast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.*
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.repository.*
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.*
import pl.llp.aircasting.util.exceptions.DBInsertException
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.extensions.showToast
import pl.llp.aircasting.util.helpers.location.LocationHelper
import pl.llp.aircasting.util.helpers.services.AveragingBackgroundService
import pl.llp.aircasting.util.helpers.services.AveragingPreviousMeasurementsBackgroundService
import pl.llp.aircasting.util.helpers.services.AveragingService

class SessionManager(
    private val mContext: Context,
    apiService: ApiService,
    private val settings: Settings
) {
    private val errorHandler = ErrorHandler(mContext)
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
    private val sessionsRespository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val measurementsRepository = MeasurementsRepository()
    private val activeSessionMeasurementsRepository = ActiveSessionMeasurementsRepository()
    private val noteRepository = NoteRepository()
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

    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(event: NewMeasurementEvent) {
        addMeasurement(event)
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
        DatabaseProvider.runQuery {
            sessionsRespository.disconnectMobileBluetoothSessions()
            sessionsRespository.finishMobileMicSessions()
        }
    }

    private fun addMeasurement(event: NewMeasurementEvent) {
        val measurementStream = MeasurementStream(event)

        val locationless = settings.areMapsDisabled()
        val lat: Double?
        val lon: Double?

        if (locationless) {
            val fakeLocation = Session.Location.FAKE_LOCATION
            lat = fakeLocation.latitude
            lon = fakeLocation.longitude
        } else {
            val location = LocationHelper.lastLocation()
            lat = location?.latitude
            lon = location?.longitude
        }
        val measurement = Measurement(event, lat, lon)

        val deviceId = event.deviceId ?: return

        DatabaseProvider.runQuery {
            val sessionId = sessionsRespository.getMobileActiveSessionIdByDeviceId(deviceId)
            sessionId?.let {
                try {
                    val measurementStreamId =
                        measurementStreamsRepository.getIdOrInsert(sessionId, measurementStream)
                    measurementsRepository.insert(measurementStreamId, sessionId, measurement)
                    activeSessionMeasurementsRepository.createOrReplace(
                        sessionId,
                        measurementStreamId,
                        measurement
                    )
                } catch (e: SQLiteConstraintException) {
                    errorHandler.handle(DBInsertException(e))
                }
            }
        }
    }

    private fun startRecording(session: Session, wifiSSID: String?, wifiPassword: String?) {
        var DBsessionId: Long? = null

        EventBus.getDefault().post(ConfigureSession(session, wifiSSID, wifiPassword))

        session.startRecording()

        if (session.isFixed()) {
            session.setFollowedAtNow()
            settings.increaseFollowedSessionsNumber()
            fixedSessionUploadService.upload(session)
        }

        DatabaseProvider.runQuery {
            DBsessionId = sessionsRespository.insert(session)
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
        DatabaseProvider.runQuery {
            val sessionId = sessionsRespository.getSessionIdByUUID(uuid)
            val session = sessionsRespository.loadSessionAndMeasurementsByUUID(uuid)
            session?.let {
                it.stopRecording()
                sessionsRespository.update(it)
                activeSessionMeasurementsRepository.deleteBySessionId(sessionId)
                sessionsSyncService.sync()
                averagingBackgroundService?.stop()
                averagingPreviousMeasurementsBackgroundService?.stop()
                AveragingService.destroy(sessionId)
            }
        }
    }

    private fun startStandaloneMode(uuid: String) {
        DatabaseProvider.runQuery {
            val sessionId = sessionsRespository.getSessionIdByUUID(uuid)
            averagingBackgroundService?.stop()
            averagingPreviousMeasurementsBackgroundService?.stop()
            AveragingService.destroy(sessionId)
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
            mContext.apply {
                showToast(
                    getString(R.string.exported_session_service_success),
                    Toast.LENGTH_LONG
                )
            }
        }
    }

    private fun deleteSession(sessionUUID: String) {
        DatabaseProvider.runQuery {
            settings.setDeletingSessionsInProgress(true)
            sessionsRespository.markForRemoval(listOf(sessionUUID))
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
        DatabaseProvider.runQuery {
            val sessionId = sessionsRespository.getSessionIdByUUID(session.uuid)
            measurementStreamsRepository.markForRemoval(sessionId, streamsToDelete)
            mCallback?.invoke()
        }
    }

    private fun updateSession(session: Session) {
        val reloadedSession = sessionsRespository.loadSessionForUpload(session.uuid)

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
        DatabaseProvider.runQuery {
            val sessionId = sessionsRespository.getSessionIdByUUID(event.session.uuid)
            sessionId?.let {
                noteRepository.insert(sessionId, event.note)
            }
        }
    }

    private fun editNote(event: NoteEditedEvent) {
        DatabaseProvider.runQuery {
            event.session?.let {
                val sessionId = sessionsRespository.getSessionIdByUUID(event.session.uuid)
                if (sessionId != null && event.note != null) {
                    noteRepository.update(sessionId, event.note)
                }
            }
            if (event.session?.endTime != null) event.session.let { session -> updateSession(session) }
        }
    }

    private fun deleteNote(event: NoteDeletedEvent) {
        DatabaseProvider.runQuery {
            event.session?.let {
                val sessionId = sessionsRespository.getSessionIdByUUID(event.session.uuid)
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
