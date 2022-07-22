package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel

abstract class SessionsObserver<Type>(
    private val mLifecycleOwner: LifecycleOwner,
    private val mSessionsViewModel: SessionsViewModel,
    private val mViewMvc: SessionsViewMvc?
) {
    private var mSessions = hashMapOf<String, Session>()
    private var mSensorThresholds = hashMapOf<String, SensorThreshold>()

    private var mSessionsLiveData: LiveData<List<Type>>? = null

    private var mObserver: Observer<List<Type>> = Observer { dbSessions ->
        DatabaseProvider.runQuery { coroutineScope ->
            val sessions = dbSessions.map { dbSession -> buildSession(dbSession) }
            val sensorThresholds = getSensorThresholds(sessions)
            if (anySensorThresholdChanged(sensorThresholds)) updateSensorThresholds(sensorThresholds)
            if (anySessionChanged(sessions)) {
                // TODO: Provide only the session whose data has been changed
                onSessionsChanged(coroutineScope, sessions)
            }
            hideLoader(coroutineScope)
        }
    }

    abstract fun buildSession(dbSession: Type): Session

    fun observe(sessionsLiveData: LiveData<List<Type>>) {
        mSessionsLiveData = sessionsLiveData
        mSessionsLiveData?.observe(mLifecycleOwner, mObserver)
    }

    fun stop() {
        mSessionsLiveData?.removeObserver(mObserver)
        mSessionsLiveData = null
    }

    private fun onSessionsChanged(coroutineScope: CoroutineScope, sessions: List<Session>) {
        if (sessions.isNotEmpty()) {
            showSessionsView(coroutineScope, sessions)
        } else {
            showEmptyView(coroutineScope)
        }

        updateSessionsCache(sessions)
    }

    private fun getSensorThresholds(sessions: List<Session>): List<SensorThreshold> {
        val streams = sessions.flatMap { it.streams }.distinctBy { it.sensorName }
        return mSessionsViewModel.findOrCreateSensorThresholds(streams)
    }

    private fun hideLoader(coroutineScope: CoroutineScope) {
        DatabaseProvider.backToUIThread(coroutineScope) {
            mViewMvc?.hideLoader()
        }
    }

    private fun showSessionsView(coroutineScope: CoroutineScope, sessions: List<Session>) {
        DatabaseProvider.backToUIThread(coroutineScope) {
            mViewMvc?.showSessionsView(sessions, mSensorThresholds)
        }
    }

    private fun showEmptyView(coroutineScope: CoroutineScope) {
        DatabaseProvider.backToUIThread(coroutineScope) {
            mViewMvc?.showEmptyView()
        }
    }

    private fun anySensorThresholdChanged(sensorThresholds: List<SensorThreshold>): Boolean {
        return mSensorThresholds.isEmpty() ||
                sensorThresholds.any { threshold -> threshold.hasChangedFrom(mSensorThresholds[threshold.sensorName]) }
    }

    private fun updateSensorThresholds(sensorThresholds: List<SensorThreshold>) {
        sensorThresholds.forEach { mSensorThresholds[it.sensorName] = it }
    }

    private fun anySessionChanged(sessions: List<Session>): Boolean {
        return mSessions.isEmpty() ||
                mSessions.size != sessions.size ||
                sessions.any { session -> session.hasChangedFrom(mSessions[session.uuid]) }
    }

    private fun updateSessionsCache(sessions: List<Session>) {
        sessions.forEach { session -> mSessions[session.uuid] = session }
    }
}
