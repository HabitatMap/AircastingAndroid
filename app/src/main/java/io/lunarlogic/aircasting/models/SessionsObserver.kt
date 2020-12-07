package io.lunarlogic.aircasting.models

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvc
import kotlinx.coroutines.CoroutineScope

class SessionsObserver(
    private val mLifecycleOwner: LifecycleOwner,
    private val mSessionsViewModel: SessionsViewModel,
    private val mViewMvc: SessionsViewMvc
) {
    private var mSessions = hashMapOf<String, Session>()
    private var mSensorThresholds = hashMapOf<String, SensorThreshold>()

    private var mSessionsLiveData: LiveData<List<SessionWithStreamsDBObject>>? = null

    private var mObserver: Observer<List<SessionWithStreamsDBObject>> = Observer { dbSessions ->
        DatabaseProvider.runQuery { coroutineScope ->
            val sessions = dbSessions.map { dbSession -> Session(dbSession) }
            val sensorThresholds = getSensorThresholds(sessions)

            hideLoader(coroutineScope)

            if (anySessionChanged(sessions) || anySensorThresholdChanged(sensorThresholds)) {
                onSessionsChanged(coroutineScope, sessions, sensorThresholds)
            }
        }
    }

    fun observe(sessionsLiveData: LiveData<List<SessionWithStreamsDBObject>>) {
        mSessionsLiveData = sessionsLiveData
        mSessionsLiveData?.observe(mLifecycleOwner, mObserver)
    }

    fun stop() {
        mSessionsLiveData?.removeObserver(mObserver)
        mSessionsLiveData = null
    }

    private fun onSessionsChanged(coroutineScope: CoroutineScope, sessions: List<Session>, sensorThresholds: List<SensorThreshold>) {
        if (sessions.size > 0) {
            updateSensorThresholds(sensorThresholds)
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
            mViewMvc.hideLoader()
        }
    }

    private fun showSessionsView(coroutineScope: CoroutineScope, sessions: List<Session>) {
        DatabaseProvider.backToUIThread(coroutineScope) {
            mViewMvc.showSessionsView(sessions, mSensorThresholds)
        }
    }

    private fun showEmptyView(coroutineScope: CoroutineScope) {
        DatabaseProvider.backToUIThread(coroutineScope) {
            mViewMvc.showEmptyView()
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
