package pl.llp.aircasting.util.helpers.sensor.handlers

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.apache.commons.lang3.time.DateUtils
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
import pl.llp.aircasting.util.helpers.location.LocationHelper
import java.util.*
import java.util.Calendar.SECOND
import java.util.concurrent.atomic.AtomicInteger

interface NewMeasurementEventObserver {
    fun observe(
        flow: SharedFlow<NewMeasurementEvent>,
        coroutineScope: CoroutineScope,
        numberOfStreams: Int = 5
    ): Job
}

class NewMeasurementEventObserverImpl(
    private val settings: Settings,
    private val errorHandler: ErrorHandler,
    private val sessionsRepository: SessionsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
) : NewMeasurementEventObserver {

    private val counter = AtomicInteger(0)
    private var timestamp: Date? = null
    private var location: Session.Location? = null

    override fun observe(
        flow: SharedFlow<NewMeasurementEvent>,
        coroutineScope: CoroutineScope,
        numberOfStreams: Int
    ) = flow.onEach { event ->
        val deviceId = event.deviceId ?: return@onEach

        val measurementStream = MeasurementStream(event)

        val measurement = Measurement(
            event,
            creationLocation(numberOfStreams),
            creationTime(numberOfStreams)
        )
        saveToDB(deviceId, measurementStream, measurement)

        counter.incrementAndGet()
    }.launchIn(coroutineScope)

    private fun creationLocation(numberOfStreams: Int): Session.Location {
        val currentLocation = Session.Location.get(
            LocationHelper.lastLocation(),
            settings.areMapsDisabled()
        )

        if (allMeasurementsFromSetReceived(numberOfStreams))
            location = currentLocation
        return location ?: currentLocation
    }

    private fun creationTime(numberOfStreams: Int): Date {
        val currentTime = currentTimeTruncatedToSeconds()
        if (allMeasurementsFromSetReceived(numberOfStreams))
            timestamp = currentTime
        return timestamp ?: currentTime
    }

    /*
    AirBeam spins out a set of 5 measurements per second in MobileActive sessions.
    To ensure that all incoming measurements from a set get the same time/location,
    we only update it when we get full set (=5 for AirBeam)
    */
    private fun allMeasurementsFromSetReceived(numberOfStreams: Int) =
        counter.get().mod(numberOfStreams) == 0

    private fun currentTimeTruncatedToSeconds() = DateUtils.truncate(Date(), SECOND)

    private suspend fun saveToDB(
        deviceId: String,
        measurementStream: MeasurementStream,
        measurement: Measurement
    ) {
        val sessionId = sessionsRepository.getMobileActiveSessionIdByDeviceId(deviceId)
        sessionId?.let {
            try {
                val measurementStreamId =
                    measurementStreamsRepository.getIdOrInsertSuspend(sessionId, measurementStream)
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