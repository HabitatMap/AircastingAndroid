package io.lunarlogic.aircasting.screens.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import io.lunarlogic.aircasting.sensor.Session

class SessionsViewModel(): ViewModel() {
    private val mDatabase = DatabaseProvider.get()

    fun loadActiveSessionsWithMeasurements(): LiveData<List<SessionWithStreamsDBObject>> {
        return loadAllByStatusWithMeasurements(Session.Status.RECORDING)
    }

    fun loadDormantSessionsWithMeasurements(): LiveData<List<SessionWithStreamsDBObject>> {
        return loadAllByStatusWithMeasurements(Session.Status.FINISHED)
    }

    private fun loadAllByStatusWithMeasurements(status: Session.Status): LiveData<List<SessionWithStreamsDBObject>> {
        return mDatabase.sessions().loadAllByStatusWithMeasurements(status)
    }
}