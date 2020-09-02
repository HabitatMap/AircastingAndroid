package io.lunarlogic.aircasting.database.repositories

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.MeasurementDBObject
import io.lunarlogic.aircasting.sensor.Measurement
import java.util.*

class MeasurementsRepository {
    private val mDatabase = DatabaseProvider.get()

    fun insert(measurementStreamId: Long, sessionId: Long, measurement: Measurement): Long {
        val measurementDBObject =
            MeasurementDBObject(
                measurementStreamId,
                sessionId,
                measurement.value,
                measurement.time,
                measurement.latitude,
                measurement.longitude
            )
        return mDatabase.measurements().insert(measurementDBObject)
    }

    fun lastMeasurementTime(sessionId: Long): Date? {
        val measurement = mDatabase.measurements().lastForSession(sessionId)
        return measurement?.time
    }
}
