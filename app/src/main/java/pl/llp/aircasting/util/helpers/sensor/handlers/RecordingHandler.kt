package pl.llp.aircasting.util.helpers.sensor.handlers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.data.api.services.FixedSessionUploadService
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.ConfigureSession
import pl.llp.aircasting.util.events.NewMeasurementEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.services.AveragingBackgroundService
import pl.llp.aircasting.util.helpers.services.AveragingPreviousMeasurementsBackgroundService
import pl.llp.aircasting.util.helpers.services.AveragingService

interface RecordingHandler {
    fun startRecording(session: Session, wifiSSID: String?, wifiPassword: String?)
    fun stopRecording(uuid: String)
    fun handle(event: NewMeasurementEvent)
    fun startStandaloneMode(uuid: String)
    fun disconnectSession(deviceId: String)
}

class RecordingHandlerImpl(
    private val coroutineScope: CoroutineScope,
    private val settings: Settings,
    private val fixedSessionUploadService: FixedSessionUploadService,
    private val sessionsRepository: SessionsRepository,
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
    private val sessionsSyncService: SessionsSyncService,
    private val errorHandler: ErrorHandler,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val measurementsRepository: MeasurementsRepositoryImpl,
    private val flows: MutableMap<String, MutableSharedFlow<NewMeasurementEvent>> = mutableMapOf(),
    private val observers: MutableMap<String, Job> = mutableMapOf(),
) : RecordingHandler {
    private var averagingPreviousMeasurementsBackgroundService
    : AveragingPreviousMeasurementsBackgroundService? = null
    private var averagingBackgroundService: AveragingBackgroundService? = null

    override fun startRecording(session: Session, wifiSSID: String?, wifiPassword: String?) {
        coroutineScope.launch {
            val databaseSessionId: Long?

            EventBus.getDefault().post(ConfigureSession(session, wifiSSID, wifiPassword))

            session.startRecording()
            databaseSessionId = sessionsRepository.insertSuspend(session)

            when (session.type) {
                Session.Type.FIXED -> {
                    session.setFollowedAtNow()
                    settings.increaseFollowedSessionsCount()
                    fixedSessionUploadService.upload(session)
                }
                Session.Type.MOBILE -> {
                    startAveragingServices(databaseSessionId)
                    startObservingNewMeasurements(session)
                }
            }
        }
    }

    private fun startAveragingServices(id: Long?) {
        id ?: return

        AveragingService.get(id)?.let { averagingService ->
            averagingBackgroundService = AveragingBackgroundService(averagingService)
            averagingBackgroundService?.start()
            averagingPreviousMeasurementsBackgroundService =
                AveragingPreviousMeasurementsBackgroundService(averagingService)
            averagingPreviousMeasurementsBackgroundService?.start()
        }
    }

    override fun handle(event: NewMeasurementEvent) {
        coroutineScope.launch {
            flows[event.deviceId]?.emit(event)
        }
    }

    private fun startObservingNewMeasurements(session: Session) {
        session.deviceId ?: return
        val handler = NewMeasurementEventObserverImpl(
            settings,
            errorHandler,
            sessionsRepository,
            measurementStreamsRepository,
            measurementsRepository,
            activeSessionMeasurementsRepository,
            session.startTime
        )

        val flow = MutableSharedFlow<NewMeasurementEvent>()
        flows[session.deviceId] = flow

        observers[session.deviceId] = handler.observe(
            flow,
            coroutineScope,
            session.defaultNumberOfStreams(),
        )
    }

    override fun stopRecording(uuid: String) {
        coroutineScope.launch {
            val sessionId = sessionsRepository.getSessionIdByUUID(uuid)

            sessionsRepository.loadSessionAndMeasurementsByUUID(uuid)?.let { session ->
                session.stopRecording()
                sessionsRepository.update(session)
                activeSessionMeasurementsRepository.deleteBySessionId(sessionId)
                sessionsSyncService.sync()
                averagingBackgroundService?.stop()
                averagingPreviousMeasurementsBackgroundService?.stop()
                AveragingService.destroy(sessionId)
                stopObservingNewMeasurements(session.deviceId)
            }
        }
    }

    override fun startStandaloneMode(uuid: String) {
        coroutineScope.launch {
            val sessionId = sessionsRepository.getSessionIdByUUID(uuid)
            averagingBackgroundService?.stop()
            averagingPreviousMeasurementsBackgroundService?.stop()
            AveragingService.destroy(sessionId)
        }
    }

    override fun disconnectSession(deviceId: String) {
        coroutineScope.launch {
            sessionsRepository.disconnectSession(deviceId)
        }
    }

    private fun stopObservingNewMeasurements(deviceId: String?) {
        observers[deviceId]?.cancel()
        observers.remove(deviceId)
        flows.remove(deviceId)
    }
}