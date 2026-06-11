package pl.llp.aircasting.util.helpers.sensor.handlers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.data.api.services.FixedSessionUploader
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.ConfigureSession
import pl.llp.aircasting.util.events.NewMeasurementEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.services.AveragingService
import pl.llp.aircasting.util.helpers.services.AveragingWindow

interface RecordingHandler {
    fun startRecording(
        session: Session,
        wifiSSID: String?,
        wifiPassword: String?,
        firmwareVersion: DeviceItem.FirmwareVersion = DeviceItem.FirmwareVersion.V1,
        intervalSeconds: Int? = null,
    )
    fun stopRecording(uuid: String)
    fun handle(event: NewMeasurementEvent)
    fun startStandaloneMode(uuid: String)
    fun disconnectSession(deviceId: String)
}

class RecordingHandlerImpl(
    private val settings: Settings,
    private val upload: FixedSessionUploader,
    private val sessionsRepository: SessionsRepository,
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
    private val sessionsSyncService: SessionsSyncService,
    private val errorHandler: ErrorHandler,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val measurementsRepository: MeasurementsRepositoryImpl,
    private val averagingService: AveragingService,
    private val coroutineScope: CoroutineScope,
    private val flows: MutableMap<String, MutableSharedFlow<NewMeasurementEvent>>,
    private val observers: MutableMap<String, Job>,
    private val v2StateRepository: AirBeamMiniV2StateRepository,
) : RecordingHandler {

    override fun startRecording(
        session: Session,
        wifiSSID: String?,
        wifiPassword: String?,
        firmwareVersion: DeviceItem.FirmwareVersion,
        intervalSeconds: Int?,
    ) {
        coroutineScope.launch {
            session.setAppropriateStatusForStartOfRecording()
            if (firmwareVersion == DeviceItem.FirmwareVersion.V2) {
                session.measurementInterval = intervalSeconds
            }
            val databaseSessionId = sessionsRepository.insert(session)

            when (session.type) {
                Session.Type.FIXED -> {
                    session.setFollowedAtNow()
                    sessionsRepository.updateFollowedAt(session)
                    settings.increaseFollowedSessionsCount()
                    val fixedSessionConfig = upload(session, firmwareVersion == DeviceItem.FirmwareVersion.V2)
                    EventBus.getDefault().post(
                        ConfigureSession(session, wifiSSID, wifiPassword, fixedSessionConfig, intervalSeconds)
                    )
                }
                Session.Type.MOBILE -> {
                    if (session.deviceType == DeviceItem.Type.AIRBEAMMINI && firmwareVersion == DeviceItem.FirmwareVersion.V2) {
                        v2StateRepository.startLocationTracking()
                    }
                    EventBus.getDefault().post(
                        ConfigureSession(session, wifiSSID, wifiPassword, intervalSeconds = intervalSeconds)
                    )
                    // Skip periodic scheduling only when the native rate is at or above
                    // the largest averaging window (60s = `AveragingWindow.SECOND.value`)
                    // — in that case no window can ever produce real averaging.
                    // For finer native rates (e.g. 5s) the schedule still runs:
                    // `startPeriodicAveraging` skips ticks where `native >= currentWindow`
                    // (no-op the 5s FIRST window for a 5s-native session) and produces
                    // real 60-second averages once the session crosses 9 hours.
                    val nativeInterval = session.measurementInterval ?: 1
                    if (nativeInterval < AveragingWindow.SECOND.value) {
                        startAveragingServices(databaseSessionId)
                    }
                    if (firmwareVersion != DeviceItem.FirmwareVersion.V2) {
                        startObservingNewMeasurements(session)
                    }
                }
            }
        }
    }

    private fun startAveragingServices(id: Long?) {
        id ?: return

        averagingService.scheduleAveraging(id)
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
            activeSessionMeasurementsRepository
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
                stopObservingNewMeasurements(session.deviceId)
                activeSessionMeasurementsRepository.deleteBySessionId(sessionId)
                averagingService.stopAndPerformFinalAveraging(uuid)
                session.stopRecording(measurementsRepository.lastMeasurementTime(sessionId))
                sessionsRepository.update(session)
                sessionsSyncService.sync()

                if (session.deviceType == DeviceItem.Type.AIRBEAMMINI && session.isMobile()) {
                    v2StateRepository.stopLocationTrackingAndClear()
                }
            }
        }
    }

    override fun startStandaloneMode(uuid: String) {
        coroutineScope.launch {
            averagingService.stopAndPerformFinalAveraging(uuid)
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