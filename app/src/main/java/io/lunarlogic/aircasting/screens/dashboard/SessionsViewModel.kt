package io.lunarlogic.aircasting.screens.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.SensorThresholdDBObject
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.SensorThreshold
import io.lunarlogic.aircasting.sensor.Session

class SessionsViewModel(): ViewModel() {
    private val mDatabase = DatabaseProvider.get()

    fun loadSessionWithMeasurements(uuid: String): LiveData<SessionWithStreamsDBObject?> {
        return mDatabase.sessions().loadLiveDataSessionAndMeasurementsByUUID(uuid)
    }

    fun loadFollowingSessionsWithMeasurements(): LiveData<List<SessionWithStreamsDBObject>> {
        return mDatabase.sessions().loadFollowingWithMeasurements()
    }

    fun loadMobileActiveSessionsWithMeasurements(): LiveData<List<SessionWithStreamsDBObject>> {
        return loadAllMobileByStatusWithMeasurements(Session.Status.RECORDING)
    }

    fun loadMobileDormantSessionsWithMeasurements(): LiveData<List<SessionWithStreamsDBObject>> {
        return loadAllMobileByStatusWithMeasurements(Session.Status.FINISHED)
    }

    fun loadFixedSessionsWithMeasurements(): LiveData<List<SessionWithStreamsDBObject>> {
        return mDatabase.sessions().loadAllByType(Session.Type.FIXED)
    }

    fun findOrCreateSensorThreshold(sensorName: String?, stream: MeasurementStream?): SensorThresholdDBObject? {
        sensorName ?: return null

        var sensorThresholdDBObject = mDatabase.sensorThresholds().findBySensorName(sensorName)

        if (sensorThresholdDBObject != null) return sensorThresholdDBObject

        stream ?: return null

        sensorThresholdDBObject = SensorThresholdDBObject(
            sensorName,
            stream.thresholdVeryLow,
            stream.thresholdLow,
            stream.thresholdMedium,
            stream.thresholdHigh,
            stream.thresholdVeryHigh
        )
        mDatabase.sensorThresholds().insert(sensorThresholdDBObject)

        return sensorThresholdDBObject
    }

    fun updateSensorThreshold(sensorThreshold: SensorThreshold) {
        mDatabase.sensorThresholds().update(
            sensorThreshold.sensorName,
            sensorThreshold.thresholdVeryLow,
            sensorThreshold.thresholdLow,
            sensorThreshold.thresholdMedium,
            sensorThreshold.thresholdHigh,
            sensorThreshold.thresholdVeryHigh
        )
    }

    private fun loadAllMobileByStatusWithMeasurements(status: Session.Status): LiveData<List<SessionWithStreamsDBObject>> {
        return mDatabase.sessions().loadAllByTypeAndStatusWithMeasurements(Session.Type.MOBILE, status)
    }
}
