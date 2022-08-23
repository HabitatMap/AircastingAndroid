package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.extensions.backToUIThread
import pl.llp.aircasting.util.extensions.runOnIOThread

abstract class SessionsObserver<Type>(
    private val mLifecycleOwner: LifecycleOwner,
    private val mSessionsViewModel: SessionsViewModel,
    private val mViewMvc: SessionsViewMvc?
) {
    enum class ModificationType {
        DELETED,
        UPDATED,
        INSERTED
    }

    private var mSessions = hashMapOf<String, Session>()
    private var mSensorThresholds = hashMapOf<String, SensorThreshold>()
    private var modifiedSessions = HashMap<ModificationType, List<Session>>()

    private var mSessionsLiveData: LiveData<List<Type>>? = null

    private var mObserver: Observer<List<Type>> = Observer { dbSessions ->
        runOnIOThread { coroutineScope ->
            val sessions = dbSessions.map { dbSession -> buildSession(dbSession) }
            val sensorThresholds = getSensorThresholds(sessions)
            if (anySensorThresholdChanged(sensorThresholds)) updateSensorThresholds(sensorThresholds)
            if (anySessionChanged(sessions)) {
                onSessionsChanged(coroutineScope, sessions)
            }
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
            val modifiedSessions = searchForModifiedSessions(sessions)
            showSessionsView(coroutineScope, modifiedSessions)
        } else {
            showEmptyView(coroutineScope)
        }

        updateSessionsCache(sessions)
    }

    private fun searchForModifiedSessions(sessions: List<Session>): Map<ModificationType, List<Session>> {
        val modified = HashMap<ModificationType, List<Session>>()
        modified[ModificationType.DELETED] = deleted(sessions)
        modified[ModificationType.INSERTED] = inserted(sessions)
        modified[ModificationType.UPDATED] = updated(sessions)
        return modified
    }

    private fun deleted(sessions: List<Session>): List<Session> {
        val deleted = mutableListOf<Session>()
        mSessions.values.forEach { old ->
            val notPresent = !sessions.contains(old)
            if (notPresent) deleted.add(old)
        }
        return deleted
    }

    private fun inserted(sessions: List<Session>): List<Session> {
        val inserted = mutableListOf<Session>()
        sessions.forEach { new ->
            val old = mSessions[new.uuid]
            if (old == null) inserted.add(new)
        }
        return inserted
    }

    private fun updated(sessions: List<Session>): List<Session> {
        val updated = mutableListOf<Session>()
        sessions.forEach { new ->
            val old = mSessions[new.uuid]
            if (old != null && new.hasChangedFrom(old)) {
                updated.add(new)
            }
        }
        return updated
    }

    private fun getSensorThresholds(sessions: List<Session>): List<SensorThreshold> {
        val streams = sessions.flatMap { it.streams }.distinctBy { it.sensorName }
        return mSessionsViewModel.findOrCreateSensorThresholds(streams)
    }

    private fun showSessionsView(
        coroutineScope: CoroutineScope,
        modifiedSessions: Map<ModificationType, List<Session>>
    ) {
        backToUIThread(coroutineScope) {
            mViewMvc?.showSessionsView(modifiedSessions, mSensorThresholds)
        }
    }

    private fun showEmptyView(coroutineScope: CoroutineScope) {
        backToUIThread(coroutineScope) {
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
