package io.lunarlogic.aircasting.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.toLiveData
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.SensorThresholdDBObject
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject

class SessionsViewModel(): ViewModel() {
    private val CONFIG = PagedList.Config.Builder()
        .setPageSize(100)
        .setPrefetchDistance(50)
        .setEnablePlaceholders(false)
        .build()
    private val mDatabase = DatabaseProvider.get()

    fun loadSessionWithMeasurements(uuid: String): LiveData<SessionWithStreamsDBObject?> {
        return mDatabase.sessions().loadLiveDataSessionAndMeasurementsByUUID(uuid)
    }

    fun loadFollowingSessionsWithMeasurements(): LiveData<PagedList<SessionWithStreamsDBObject>> {
        return mDatabase.sessions()
            .loadFollowingWithMeasurements()
            .toLiveData(CONFIG)
    }

    fun loadMobileActiveSessionsWithMeasurements(): LiveData<PagedList<SessionWithStreamsDBObject>> {
        return loadAllMobileByStatusWithMeasurements(Session.Status.RECORDING)
    }

    fun loadMobileDormantSessionsWithMeasurements(): LiveData<PagedList<SessionWithStreamsDBObject>> {
        return loadAllMobileByStatusWithMeasurements(Session.Status.FINISHED)
    }

    fun loadFixedSessionsWithMeasurements(): LiveData<PagedList<SessionWithStreamsDBObject>> {
        return mDatabase.sessions()
            .loadAllByType(Session.Type.FIXED)
            .toLiveData(CONFIG)
    }

    fun findOrCreateSensorThresholds(session: Session): List<SensorThreshold> {
        return findOrCreateSensorThresholds(session.streams)
    }

    fun findOrCreateSensorThresholds(streams: List<MeasurementStream>): List<SensorThreshold> {
        val existingThresholds = findSensorThresholds(streams)
        var newThresholds = createSensorThresholds(streams, existingThresholds)

        val thresholds = mutableListOf<SensorThreshold>()
        thresholds.addAll(existingThresholds)
        thresholds.addAll(newThresholds)

        return thresholds
    }

    private fun findSensorThresholds(streams: List<MeasurementStream>): List<SensorThreshold> {
        val sensorNames = streams.map { it.sensorName }
        return mDatabase.sensorThresholds()
            .allBySensorNames(sensorNames)
            .map { SensorThreshold(it) }
    }

    private fun createSensorThresholds(streams: List<MeasurementStream>, existingThreshols: List<SensorThreshold>): List<SensorThreshold> {
        val existingSensorNames = existingThreshols.map { it.sensorName }
        val toCreate = streams.filter { !existingSensorNames.contains(it.sensorName) }

        return toCreate.map { stream ->
            val sensorThresholdDBObject = SensorThresholdDBObject(stream)
            mDatabase.sensorThresholds().insert(sensorThresholdDBObject)
            SensorThreshold(sensorThresholdDBObject)
        }
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

    fun updateFollowedAt(session: Session) {
        mDatabase.sessions().updateFollowedAt(session.uuid, session.followedAt)
    }

    private fun loadAllMobileByStatusWithMeasurements(status: Session.Status): LiveData<PagedList<SessionWithStreamsDBObject>> {
        return mDatabase.sessions()
            .loadAllByTypeAndStatusWithMeasurements(Session.Type.MOBILE, status)
            .toLiveData(CONFIG)
    }
}
