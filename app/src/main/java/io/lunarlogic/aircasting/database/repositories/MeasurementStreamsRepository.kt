package io.lunarlogic.aircasting.database.repositories

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.MeasurementStreamDBObject
import io.lunarlogic.aircasting.sensor.MeasurementStream

class MeasurementStreamsRepository {
    private val mDatabase = DatabaseProvider.get()

    fun getIdOrInsert(sessionId: Long, measurementStream: MeasurementStream): Long {
        var streamDBObject = mDatabase.measurementStreams().
            loadStreamBySessionIdAndSensorName(sessionId, measurementStream.sensorName)

        if (streamDBObject != null) return streamDBObject.id

        streamDBObject = MeasurementStreamDBObject(
            sessionId,
            measurementStream
        )
        return mDatabase.measurementStreams().insert(streamDBObject)
    }
}