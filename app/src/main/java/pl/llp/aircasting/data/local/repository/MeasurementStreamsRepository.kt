package pl.llp.aircasting.data.local.repository

import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.entity.MeasurementStreamDBObject
import pl.llp.aircasting.data.model.MeasurementStream

class MeasurementStreamsRepository {
    private val mDatabase = DatabaseProvider.get()

    fun getId(sessionId: Long, measurementStream: MeasurementStream): Long? {
        val streamDBObject = mDatabase.measurementStreams()
            .loadStreamBySessionIdAndSensorName(sessionId, measurementStream.sensorName)

        return streamDBObject?.id
    }

    fun getIdOrInsert(sessionId: Long, measurementStream: MeasurementStream): Long {
        var streamDBObject = mDatabase.measurementStreams()
            .loadStreamBySessionIdAndSensorName(sessionId, measurementStream.sensorName)

        if (streamDBObject != null) return streamDBObject.id

        streamDBObject = MeasurementStreamDBObject(
            sessionId,
            measurementStream
        )
        return mDatabase.measurementStreams().insert(streamDBObject)
    }

    suspend fun getIdOrInsertSuspend(sessionId: Long, measurementStream: MeasurementStream): Long {
        var streamDBObject = mDatabase.measurementStreams()
            .loadStreamBySessionIdAndSensorNameSuspend(sessionId, measurementStream.sensorName)

        if (streamDBObject != null) return streamDBObject.id

        streamDBObject = MeasurementStreamDBObject(
            sessionId,
            measurementStream
        )
        return mDatabase.measurementStreams().insertSuspend(streamDBObject)
    }

    fun insert(sessionId: Long, streams: List<MeasurementStream>) {
        streams.forEach { stream ->
            val streamDBObject = MeasurementStreamDBObject(sessionId, stream)
            mDatabase.measurementStreams().insert(streamDBObject)
        }
    }

    fun insert(sessionId: Long, stream: MeasurementStream): Long {
        val streamDBObject = MeasurementStreamDBObject(sessionId, stream)
        return mDatabase.measurementStreams().insert(streamDBObject)
    }

    fun markForRemoval(sessionId: Long?, streamsToDelete: List<MeasurementStream>?) {
        streamsToDelete?.forEach { stream ->
            if (sessionId != null) {
                mDatabase.measurementStreams().markForRemoval(sessionId, stream.sensorName)
            }
        }
    }

    fun deleteMarkedForRemoval() {
        mDatabase.measurementStreams().deleteMarkedForRemoval()
    }

    fun getStreamsIdsBySessionIds(sessionsIds: List<Long>): List<Long> {
        return mDatabase.measurementStreams().getStreamsIdsBySessionIds(sessionsIds)
    }

    suspend fun getStreamsIdsBySessionId(sessionId: Long): List<Long> {
        return mDatabase.measurementStreams().getStreamsIdsBySessionId(sessionId)
    }
}
