package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import kotlinx.coroutines.CoroutineScope

abstract class SessionsObserver<Type>(
    private val mLifecycleOwner: LifecycleOwner,
    private val mSessionsViewModel: SessionsViewModel,
    private val mViewMvc: SessionsViewMvc?
) {
    private var mSessions = hashMapOf<String, LocalSession>()
    private var mSensorThresholds = hashMapOf<String, SensorThreshold>()

    private var mSessionsLiveData: LiveData<List<Type>>? = null

    private var mObserver: Observer<List<Type>> = Observer { dbSessions ->
        DatabaseProvider.runQuery { coroutineScope ->
            val sessions = dbSessions.map { dbSession -> buildSession(dbSession) }
            val sensorThresholds = getSensorThresholds(sessions)
            if (anySessionChanged(sessions) || anySensorThresholdChanged(sensorThresholds)) {
                onSessionsChanged(coroutineScope, sessions, sensorThresholds)
            }
            hideLoader(coroutineScope)
        }
    }

    abstract fun buildSession(dbSession: Type): LocalSession

    fun observe(sessionsLiveData: LiveData<List<Type>>) {
        mSessionsLiveData = sessionsLiveData
        mSessionsLiveData?.observe(mLifecycleOwner, mObserver)
    }

    fun stop() {
        mSessionsLiveData?.removeObserver(mObserver)
        mSessionsLiveData = null
    }

    private fun onSessionsChanged(coroutineScope: CoroutineScope, localSessions: List<LocalSession>, sensorThresholds: List<SensorThreshold>) {
        if (localSessions.isNotEmpty()) {
            updateSensorThresholds(sensorThresholds)
            showSessionsView(coroutineScope, localSessions)
        } else {
            showEmptyView(coroutineScope)
        }

        updateSessionsCache(localSessions)
    }

    private fun getSensorThresholds(localSessions: List<LocalSession>): List<SensorThreshold> {
        val streams = localSessions.flatMap { it.streams }.distinctBy { it.sensorName }
        return mSessionsViewModel.findOrCreateSensorThresholds(streams)
    }

    private fun hideLoader(coroutineScope: CoroutineScope) {
        DatabaseProvider.backToUIThread(coroutineScope) {
            mViewMvc?.hideLoader()
        }
    }

    private fun showSessionsView(coroutineScope: CoroutineScope, localSessions: List<LocalSession>) {
        DatabaseProvider.backToUIThread(coroutineScope) {
            mViewMvc?.showSessionsView(localSessions, mSensorThresholds)
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

    private fun anySessionChanged(localSessions: List<LocalSession>): Boolean {
        return mSessions.isEmpty() ||
                mSessions.size != localSessions.size ||
                localSessions.any { session -> session.hasChangedFrom(mSessions[session.uuid]) }
    }

    private fun updateSessionsCache(localSessions: List<LocalSession>) {
        localSessions.forEach { session -> mSessions[session.uuid] = session }
    }
}
