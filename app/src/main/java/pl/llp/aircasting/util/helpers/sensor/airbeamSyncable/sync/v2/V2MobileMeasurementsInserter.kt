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
    // Per-sync cache. The fallback location and the two stream IDs are resolved ONCE per
    // session, not per chunk. Previously lastKnownLocation() ran a `session_id + ORDER BY
    // time` measurements scan on every chunk — O(session size) per call, i.e. O(n²) over a
    // long sync — which made the drain progressively slower (tens of minutes for a large
    // backlog). getIdOrInsert was likewise re-queried per chunk. The manual-sync consumer is
    // a single serialized coroutine, so a plain cache keyed by session id is safe here.
    private var cachedSessionId: Long = -1L
    private var cachedFallback: Session.Location = DEFAULT_LOCATION
    private var cachedPm1StreamId: Long = -1L
    private var cachedPm25StreamId: Long = -1L

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

        if (session.id != cachedSessionId) {
            cachedSessionId = session.id
            cachedFallback = lastKnownLocation(session.id)
            cachedPm1StreamId = measurementStreamsRepository.getIdOrInsert(session.id, pm1Stream(deviceId))
            cachedPm25StreamId = measurementStreamsRepository.getIdOrInsert(session.id, pm25Stream(deviceId))
        }
        val fallback = cachedFallback

        // Synced records can span the whole session and overlap rows the live path already
        // inserted; the unique (session_id, stream_id, time) index + @Insert(IGNORE) dedupes.
        val pm1 = measurements.map {
            val loc = v2StateRepository.getClosestLocation(it.timestamp.time, fallback)
            Measurement(it.pm1.toDouble(), it.timestamp, loc.latitude, loc.longitude)
        }
        measurementsRepository.insertAll(cachedPm1StreamId, session.id, pm1)

        val pm25 = measurements.map {
            val loc = v2StateRepository.getClosestLocation(it.timestamp.time, fallback)
            Measurement(it.pm25.toDouble(), it.timestamp, loc.latitude, loc.longitude)
        }
        measurementsRepository.insertAll(cachedPm25StreamId, session.id, pm25)

        Log.d(TAG, "V2MobileInserter: inserted ${measurements.size} records (PM1+PM2.5)")
    }

    private fun pm1Stream(deviceId: String) = MeasurementStream(
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
    )

    private fun pm25Stream(deviceId: String) = MeasurementStream(
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
    )

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
