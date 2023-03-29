package pl.llp.aircasting.data.local.repository

import pl.llp.aircasting.data.local.AppDatabase
import pl.llp.aircasting.data.local.entity.SensorThresholdDBObject
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.UserSessionScope
import javax.inject.Inject

@UserSessionScope
class ThresholdsRepository @Inject constructor(
    private val mDatabase: AppDatabase
) {
    suspend fun findOrCreateSensorThresholds(session: Session): List<SensorThreshold> {
        return findOrCreateSensorThresholds(session.streams)
    }

    suspend fun findOrCreateSensorThresholds(streams: List<MeasurementStream>): List<SensorThreshold> {
        val existingThresholds = findSensorThresholds(streams)
        val newThresholds = insertNewSensorThresholds(streams, existingThresholds)

        val thresholds = mutableListOf<SensorThreshold>()
        thresholds.addAll(existingThresholds)
        thresholds.addAll(newThresholds)

        return thresholds
    }

    private suspend fun findSensorThresholds(streams: List<MeasurementStream>): List<SensorThreshold> {
        val sensorNames = streams.map { it.sensorName }
        return mDatabase.sensorThresholds()
            .allBySensorNames(sensorNames)
            .map { SensorThreshold(it) }
    }

    private suspend fun insertNewSensorThresholds(streams: List<MeasurementStream>, existingThresholds: List<SensorThreshold>): List<SensorThreshold> {
        val existingSensorNames = existingThresholds.map { it.sensorName }
        val toCreate = streams.filter { !existingSensorNames.contains(it.sensorName) }

        return toCreate.map { stream ->
            val sensorThresholdDBObject = SensorThresholdDBObject(stream)
            mDatabase.sensorThresholds().insert(sensorThresholdDBObject)
            SensorThreshold(sensorThresholdDBObject)
        }
    }

    suspend fun updateSensorThreshold(sensorThreshold: SensorThreshold) {
        mDatabase.sensorThresholds().update(
            sensorThreshold.sensorName,
            sensorThreshold.thresholdVeryLow,
            sensorThreshold.thresholdLow,
            sensorThreshold.thresholdMedium,
            sensorThreshold.thresholdHigh,
            sensorThreshold.thresholdVeryHigh
        )
    }

}