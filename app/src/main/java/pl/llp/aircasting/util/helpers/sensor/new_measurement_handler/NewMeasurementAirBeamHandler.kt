package pl.llp.aircasting.util.helpers.sensor.new_measurement_handler

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
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
import pl.llp.aircasting.util.helpers.location.LocationHelper
import pl.llp.aircasting.util.helpers.sensor.microphone.MicrophoneDeviceItem
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

open class NewMeasurementAirBeamHandler(
    private val settings: Settings,
    private val errorHandler: ErrorHandler,
    private val sessionsRepository: SessionsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
) {
    private val counter = AtomicInteger(0)
    private lateinit var timestamp: Date
    protected open val numberOfStreams = 5

    fun handle(event: NewMeasurementEvent) {
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

        val measurement =
            if (measurementStream.detailedType == MicrophoneDeviceItem.DETAILED_TYPE)
                Measurement(event, lat, lon)
            else
                Measurement(event, lat, lon, creationTime())
        Log.v(TAG, "Measurement time: ${measurement.time}")

        val deviceId = event.deviceId ?: return

        saveToDB(deviceId, measurementStream, measurement)
    }

    private fun creationTime(): Date {
        if (counter.getAndIncrement().mod(5) == 0)
            timestamp = Date()
        return timestamp
    }

    private fun saveToDB(
        deviceId: String,
        measurementStream: MeasurementStream,
        measurement: Measurement
    ) {
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
    }
}