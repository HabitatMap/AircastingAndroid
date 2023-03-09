package pl.llp.aircasting.util.helpers.sensor.handlers

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
import pl.llp.aircasting.util.extensions.addSeconds
import pl.llp.aircasting.util.extensions.calendar
import pl.llp.aircasting.util.extensions.truncateTo
import pl.llp.aircasting.util.helpers.location.LocationHelper
import java.util.*
import java.util.Calendar.SECOND
import java.util.concurrent.atomic.AtomicInteger

interface NewMeasurementEventObserver {
    fun observe(
        flow: SharedFlow<NewMeasurementEvent>,
        coroutineScope: CoroutineScope,
        numberOfStreams: Int = 5,
    ): Job
}

class NewMeasurementEventObserverImpl(
    private val settings: Settings,
    private val errorHandler: ErrorHandler,
    private val sessionsRepository: SessionsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository
) : NewMeasurementEventObserver {

    private var timestamp = Date().truncateTo(SECOND)
    private val counter = AtomicInteger(0)
    private var location = Session.Location.get(
        LocationHelper.lastLocation(),
        settings.areMapsDisabled()
    )

    override fun observe(
        flow: SharedFlow<NewMeasurementEvent>,
        coroutineScope: CoroutineScope,
        numberOfStreams: Int,
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
        if (allMeasurementsFromSetReceived(numberOfStreams))
            location = Session.Location.get(
                LocationHelper.lastLocation(),
                settings.areMapsDisabled()
            )
        return location
    }

    private fun creationTime(numberOfStreams: Int): Date {
        if (allMeasurementsFromSetReceived(numberOfStreams))
            timestamp = calendar().addSeconds(timestamp, 1) ?: timestamp
        return timestamp
    }

    /*
    AirBeam spins out a set of 5 measurements per second in MobileActive sessions.
    To ensure that all incoming measurements from a set get the same time/location,
    we only update it when we get full set (=5 for AirBeam)
    */
    private fun allMeasurementsFromSetReceived(numberOfStreams: Int) =
        counter.get().mod(numberOfStreams) == 0

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