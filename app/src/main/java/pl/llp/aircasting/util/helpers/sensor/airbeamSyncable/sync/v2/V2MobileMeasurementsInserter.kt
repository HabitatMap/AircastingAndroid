package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2

import android.util.Log
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.Session.Location.Companion.DEFAULT_LOCATION
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.helpers.location.LocationHelper
import pl.llp.aircasting.util.helpers.location.toLatLng
import javax.inject.Inject

/**
 * V2 counterpart of [pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor.SDCardMobileSessionsProcessor].
 *
 * Mirrors the V1 SD-card mobile sync logic: skip already-finished sessions, attach the
 * last known location to every record, and only insert measurements whose timestamp is
 * newer than what's already in the DB for the matching stream. The V2 wire format does
 * not need CSV parsing, so this class consumes parsed [V2SyncMeasurement]s directly.
 */
@UserSessionScope
class V2MobileMeasurementsInserter @Inject constructor(
    private val sessionsRepository: SessionsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val measurementsRepository: MeasurementsRepository,
) {
    suspend fun insert(
        deviceId: String,
        session: SessionDBObject,
        measurements: List<V2SyncMeasurement>,
    ) {
        if (session.type != Session.Type.MOBILE) {
            Log.w(TAG, "V2MobileInserter: refusing non-mobile session ${session.uuid}")
            return
        }
        if (session.isFinished) {
            Log.d(TAG, "V2MobileInserter: skipping finished session ${session.uuid}")
            return
        }
        if (measurements.isEmpty()) return

        val location = lastKnownLocation(session.id)

        insertStream(
            session.id,
            stream = MeasurementStream(
                sensorPackageName = "AirBeamMini:$deviceId",
                sensorName = "AirBeamMini-PM1",
                measurementType = "Particulate Matter",
                measurementShortType = "PM",
                unitName = "microgram per cubic meter",
                unitSymbol = "µg/m³",
                thresholdVeryLow = 0,
                thresholdLow = 9,
                thresholdMedium = 35,
                thresholdHigh = 55,
                thresholdVeryHigh = 150,
            ),
            measurements.map { Measurement(it.pm1.toDouble(), it.timestamp, location.latitude, location.longitude) },
        )

        insertStream(
            session.id,
            stream = MeasurementStream(
                sensorPackageName = "AirBeamMini:$deviceId",
                sensorName = "AirBeamMini-PM2.5",
                measurementType = "Particulate Matter",
                measurementShortType = "PM",
                unitName = "microgram per cubic meter",
                unitSymbol = "µg/m³",
                thresholdVeryLow = 0,
                thresholdLow = 9,
                thresholdMedium = 35,
                thresholdHigh = 55,
                thresholdVeryHigh = 150,
            ),
            measurements.map { Measurement(it.pm25.toDouble(), it.timestamp, location.latitude, location.longitude) },
        )
    }

    private suspend fun insertStream(
        sessionId: Long,
        stream: MeasurementStream,
        candidates: List<Measurement>,
    ) {
        val streamId = measurementStreamsRepository.getIdOrInsert(sessionId, stream)
        val lastTime = measurementsRepository.lastMeasurementTime(sessionId, streamId)
        val toInsert = if (lastTime == null) candidates else candidates.filter { it.time > lastTime }
        if (toInsert.isEmpty()) {
            Log.d(TAG, "V2MobileInserter: ${stream.sensorName} — nothing newer than $lastTime")
            return
        }
        Log.d(TAG, "V2MobileInserter: inserting ${toInsert.size} of ${candidates.size} for ${stream.sensorName}")
        measurementsRepository.insertAll(streamId, sessionId, toInsert)
    }

    private suspend fun lastKnownLocation(sessionId: Long): Session.Location {
        val streamCoords = measurementStreamsRepository.getLastKnownLatLng(sessionId)
        val streamLat = streamCoords?.latitude
        val streamLon = streamCoords?.longitude
        if (streamLat != null && streamLon != null) {
            return Session.Location(streamLat, streamLon)
        }
        val device = LocationHelper.lastLocation()?.toLatLng()
        val deviceLat = device?.latitude
        val deviceLon = device?.longitude
        if (deviceLat != null && deviceLon != null) {
            return Session.Location(deviceLat, deviceLon)
        }
        return DEFAULT_LOCATION
    }
}
