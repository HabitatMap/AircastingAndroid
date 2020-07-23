package io.lunarlogic.aircasting.screens.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import io.lunarlogic.aircasting.sensor.Session

class SessionsViewModel(): ViewModel() {
    private val mDatabase = DatabaseProvider.get()

    fun loadFollowingSessionsWithMeasurements(): LiveData<List<SessionWithStreamsDBObject>> {
        // TODO: handle properly after implementing following feature
        return loadFixedSessionsWithMeasurements()
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

    private fun loadAllMobileByStatusWithMeasurements(status: Session.Status): LiveData<List<SessionWithStreamsDBObject>> {
        return mDatabase.sessions().loadAllByTypeAndStatusWithMeasurements(Session.Type.MOBILE, status)
    }
}