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
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository

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
    private val v2StateRepository: AirBeamMiniV2StateRepository,
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

        val fallbackLocation = lastKnownLocation(session.id)

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
            measurements.map {
                val location = v2StateRepository.getClosestLocation(it.timestamp.time, fallbackLocation)
                Measurement(it.pm1.toDouble(), it.timestamp, location.latitude, location.longitude)
            },
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
            measurements.map {
                val location = v2StateRepository.getClosestLocation(it.timestamp.time, fallbackLocation)
                Measurement(it.pm25.toDouble(), it.timestamp, location.latitude, location.longitude)
            },
        )
    }

    private suspend fun insertStream(
        sessionId: Long,
        stream: MeasurementStream,
        candidates: List<Measurement>,
    ) {
        val streamId = measurementStreamsRepository.getIdOrInsert(sessionId, stream)
        if (candidates.isEmpty()) return
        // Synced records can span the entire session (firmware writes every measurement to
        // flash regardless of BLE state), so they overlap arbitrarily with rows the live
        // path already inserted. Hand the full batch to the DAO and let the unique index
        // on (session_id, stream_id, time) + `@Insert(OnConflictStrategy.IGNORE)` drop
        // duplicates. Filtering by `time > lastTime` here would silently skip BLE-gap
        // fill-ins that arrive out-of-order on the Sync-and-Finish path.
        Log.d(TAG, "V2MobileInserter: inserting ${candidates.size} candidates for ${stream.sensorName} (DB dedupes via unique index)")
        measurementsRepository.insertAll(streamId, sessionId, candidates)
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
