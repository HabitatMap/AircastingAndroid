package pl.llp.aircasting.database.repositories

import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.data_classes.MeasurementStreamDBObject
import pl.llp.aircasting.models.MeasurementStream

class MeasurementStreamsRepository {
    val context = AircastingApplication.appContext
    private val mDatabase = DatabaseProvider.get(context)

    fun getId(sessionId: Long, measurementStream: MeasurementStream): Long? {
        val streamDBObject = mDatabase.measurementStreams()
            .loadStreamBySessionIdAndSensorName(sessionId, measurementStream.sensorName)

        return streamDBObject?.id
    }

    fun getIdOrInsert(sessionId: Long, measurementStream: MeasurementStream): Long {
        var streamDBObject = mDatabase.measurementStreams()
            .loadStreamBySessionIdAndSensorName(sessionId, measurementStream.sensorName)

        if (streamDBObject != null) return streamDBObject.id
        streamDBObject = MeasurementStreamDBObject(sessionId, measurementStream)
        return mDatabase.measurementStreams().insert(streamDBObject)
    }

    fun insert(sessionId: Long, streams: List<MeasurementStream>) {
        streams.forEach { stream ->
            val streamDBObject = MeasurementStreamDBObject(sessionId, stream)
            mDatabase.measurementStreams().insert(streamDBObject)
        }
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
}
