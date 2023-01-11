package pl.llp.aircasting.util.helpers.sensor.handlers

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.data.api.services.FixedSessionUploadService
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
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
    private val measurementsRepository: MeasurementsRepository,
    private val airBeamNewMeasurementEventFlow: MutableSharedFlow<NewMeasurementEvent>,
    private val microphoneNewMeasurementEventFlow: MutableSharedFlow<NewMeasurementEvent>,

    private val airBeamNewMeasurementEventHandler: NewMeasurementEventHandler = AirBeamNewMeasurementEventHandler(
        settings,
        errorHandler,
        sessionsRepository,
        measurementStreamsRepository,
        measurementsRepository,
        activeSessionMeasurementsRepository
    ),
    private val microphoneNewMeasurementEventHandler: NewMeasurementEventHandler = MicrophoneNewMeasurementEventHandler(
        settings,
        errorHandler,
        sessionsRepository,
        measurementStreamsRepository,
        measurementsRepository,
        activeSessionMeasurementsRepository
    ),
) : RecordingHandler {
    private var averagingPreviousMeasurementsBackgroundService: AveragingPreviousMeasurementsBackgroundService? =
        null
    private var averagingBackgroundService: AveragingBackgroundService? = null
    private lateinit var microphoneNewMeasurementObserver: Job
    private lateinit var airBeamNewMeasurementObserver: Job

    override fun startRecording(session: Session, wifiSSID: String?, wifiPassword: String?) {
        val databaseSessionId: Long?

        EventBus.getDefault().post(ConfigureSession(session, wifiSSID, wifiPassword))

        session.startRecording()
        databaseSessionId = sessionsRepository.insert(session)

        when (session.type) {
            Session.Type.FIXED -> {
                session.setFollowedAtNow()
                settings.increaseFollowedSessionsCount()
                fixedSessionUploadService.upload(session)
            }
            Session.Type.MOBILE -> {
                startAveragingServices(databaseSessionId)
                startObservingNewMeasurements(session.deviceType)
            }
        }
    }

    private fun startAveragingServices(it: Long) {
        AveragingService.get(it)?.let { averagingService ->
            averagingBackgroundService = AveragingBackgroundService(averagingService)
            averagingBackgroundService?.start()
            averagingPreviousMeasurementsBackgroundService =
                AveragingPreviousMeasurementsBackgroundService(averagingService)
            averagingPreviousMeasurementsBackgroundService?.start()
        }
    }

    private fun startObservingNewMeasurements(deviceType: DeviceItem.Type?) = when (deviceType) {
        DeviceItem.Type.MIC -> microphoneNewMeasurementObserver =
            microphoneNewMeasurementEventFlow.onEach {
                Log.v(
                    TAG,
                    "AirBeam NewMeasurement: ${it.measuredValue} ${it.measurementShortType}"
                )
                airBeamNewMeasurementEventHandler.handle(it)
            }.launchIn(coroutineScope)

        else -> airBeamNewMeasurementObserver = airBeamNewMeasurementEventFlow.onEach {
            Log.v(TAG, "AirBeam NewMeasurement: ${it.measuredValue} ${it.measurementShortType}")
            airBeamNewMeasurementEventHandler.handle(it)
        }.launchIn(coroutineScope)
    }

    override fun stopRecording(uuid: String) {
        val sessionId = sessionsRepository.getSessionIdByUUID(uuid)

        sessionsRepository.loadSessionAndMeasurementsByUUID(uuid)?.let { session ->
            session.stopRecording()
            sessionsRepository.update(session)
            activeSessionMeasurementsRepository.deleteBySessionId(sessionId)
            sessionsSyncService.sync()
            averagingBackgroundService?.stop()
            averagingPreviousMeasurementsBackgroundService?.stop()
            AveragingService.destroy(sessionId)
            stopObservingMeasurements(session)
        }
    }

    private fun stopObservingMeasurements(session: Session) = when (session.deviceType) {
        DeviceItem.Type.MIC -> {
            microphoneNewMeasurementObserver.cancel()
            microphoneNewMeasurementEventHandler.reset()
        }
        else -> {
            airBeamNewMeasurementObserver.cancel()
            airBeamNewMeasurementEventHandler.reset()
        }
    }
}