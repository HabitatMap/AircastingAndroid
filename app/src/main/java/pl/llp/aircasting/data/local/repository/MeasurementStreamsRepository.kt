package pl.llp.aircasting.data.local.repository

import pl.llp.aircasting.data.local.AppDatabase
import pl.llp.aircasting.data.local.entity.LatLng
import pl.llp.aircasting.data.local.entity.MeasurementStreamDBObject
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.di.UserSessionScope
import javax.inject.Inject

@UserSessionScope
class MeasurementStreamsRepository @Inject constructor(
    private val mDatabase: AppDatabase
) {
    suspend fun getId(sessionId: Long, measurementStream: MeasurementStream): Long? {
        val streamDBObject = mDatabase.measurementStreams()
            .loadStreamBySessionIdAndSensorName(sessionId, measurementStream.sensorName)

        return streamDBObject?.id
    }

    suspend fun getSessionStreams(sessionId: Long) =
        mDatabase.measurementStreams().getSessionStreams(sessionId)

    suspend fun delete(streams: List<MeasurementStreamDBObject>) =
        mDatabase.measurementStreams().delete(streams)

    suspend fun getIdOrInsert(sessionId: Long, measurementStream: MeasurementStream): Long {
        var streamDBObject = mDatabase.measurementStreams()
            .loadStreamBySessionIdAndSensorName(sessionId, measurementStream.sensorName)

        if (streamDBObject != null) return streamDBObject.id

        streamDBObject = MeasurementStreamDBObject(
            sessionId,
            measurementStream
        )
        return mDatabase.measurementStreams().insert(streamDBObject)
    }

    suspend fun getIdOrInsert(sessionId: Long, measurementStream: MeasurementStreamDBObject): Long {
        return mDatabase.measurementStreams()
            .loadStreamBySessionIdAndSensorName(sessionId, measurementStream.sensorName)
            ?.id
            ?: mDatabase.measurementStreams().insert(measurementStream)
    }

    suspend fun insert(sessionId: Long, streams: List<MeasurementStream>) {
        streams.forEach { stream ->
            val streamDBObject = MeasurementStreamDBObject(sessionId, stream)
            mDatabase.measurementStreams().insert(streamDBObject)
        }
    }

    suspend fun insert(sessionId: Long, stream: MeasurementStream): Long {
        val streamDBObject = MeasurementStreamDBObject(sessionId, stream)
        return mDatabase.measurementStreams().insert(streamDBObject)
    }

    suspend fun markForRemoval(sessionId: Long?, streamsToDelete: List<MeasurementStream>?) {
        streamsToDelete?.forEach { stream ->
            if (sessionId != null) {
                mDatabase.measurementStreams().markForRemoval(sessionId, stream.sensorName)
            }
        }
    }

    suspend fun deleteMarkedForRemoval() {
        mDatabase.measurementStreams().deleteMarkedForRemoval()
    }

    suspend fun getStreamsIdsBySessionIds(sessionsIds: List<Long>): List<Long> {
        return mDatabase.measurementStreams().getStreamsIdsBySessionIds(sessionsIds)
    }

    suspend fun getStreamsIdsBySessionId(sessionId: Long): List<Long> {
        return mDatabase.measurementStreams().getStreamsIdsBySessionId(sessionId)
    }

    suspend fun getLastKnownLatLng(sessionId: Long): LatLng? {
        return mDatabase.measurements().lastKnownLatLng(sessionId)
    }
}
