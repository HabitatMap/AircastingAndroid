package io.lunarlogic.aircasting.database.repositories

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.MeasurementDBObject
import io.lunarlogic.aircasting.sensor.Measurement

class MeasurementsRepository {
    private val mDatabase = DatabaseProvider.get()

    fun insert(measurementStreamId: Long, measurement: Measurement): Long {
        val measurementDBObject =
            MeasurementDBObject(
                measurementStreamId,
                measurement.value,
                measurement.time
            )
        return mDatabase.measurements().insert(measurementDBObject)
    }
}