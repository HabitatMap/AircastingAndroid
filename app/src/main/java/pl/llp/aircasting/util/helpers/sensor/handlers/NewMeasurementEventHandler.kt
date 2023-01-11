package pl.llp.aircasting.util.helpers.sensor.handlers

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.apache.commons.lang3.time.DateUtils
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.NewMeasurementEvent
import pl.llp.aircasting.util.exceptions.DBInsertException
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.runOnIOThread
import pl.llp.aircasting.util.helpers.location.LocationHelper
import java.util.*
import java.util.Calendar.SECOND
import java.util.concurrent.atomic.AtomicInteger


abstract class NewMeasurementEventHandler(
    private val settings: Settings,
    private val errorHandler: ErrorHandler,
    private val sessionsRepository: SessionsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
) {
    abstract val numberOfStreams: Int

    private val counter = AtomicInteger(0)
    private lateinit var timestamp: Date

    private lateinit var job: Job

    fun observe(flow: SharedFlow<NewMeasurementEvent>, coroutineScope: CoroutineScope) {
        job = flow.onEach { event ->
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

            val measurement = Measurement(event, lat, lon, creationTime())
            Log.v(TAG, "Measurement time: ${measurement.time}")

            val deviceId = event.deviceId ?: return@onEach

            saveToDB(deviceId, measurementStream, measurement)
        }.launchIn(coroutineScope)
    }

    private fun creationTime(): Date {
        if (counter.getAndIncrement().mod(5) == 0)
            timestamp = currentTimeTruncatedToSeconds()
        return timestamp
    }

    private fun currentTimeTruncatedToSeconds() = DateUtils.truncate(Date(), SECOND)

    private fun saveToDB(
        deviceId: String,
        measurementStream: MeasurementStream,
        measurement: Measurement
    ) = runOnIOThread {
        val sessionId = sessionsRepository.getMobileActiveSessionIdByDeviceId(deviceId)
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

    fun reset() {
        counter.set(0)
        job.cancel()
    }
}

class AirBeamNewMeasurementEventHandler(
    settings: Settings,
    errorHandler: ErrorHandler,
    sessionsRepository: SessionsRepository,
    measurementStreamsRepository: MeasurementStreamsRepository,
    measurementsRepository: MeasurementsRepository,
    activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
) : NewMeasurementEventHandler(
    settings,
    errorHandler,
    sessionsRepository,
    measurementStreamsRepository,
    measurementsRepository,
    activeSessionMeasurementsRepository
) {
    override val numberOfStreams: Int
        get() = 5
}

open class MicrophoneNewMeasurementEventHandler(
    settings: Settings,
    errorHandler: ErrorHandler,
    sessionsRepository: SessionsRepository,
    measurementStreamsRepository: MeasurementStreamsRepository,
    measurementsRepository: MeasurementsRepository,
    activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
) : NewMeasurementEventHandler(
    settings,
    errorHandler,
    sessionsRepository,
    measurementStreamsRepository,
    measurementsRepository,
    activeSessionMeasurementsRepository
) {
    override val numberOfStreams: Int
        get() = 1
}