package pl.llp.aircasting.ui.view.screens.dashboard

import android.view.LayoutInflater
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel

abstract class SessionsRecyclerAdapter<ListenerType>(
    private val mInflater: LayoutInflater,
    protected val supportFragmentManager: FragmentManager
): RecyclerView.Adapter<SessionsRecyclerAdapter<ListenerType>.MyViewHolder>() {
    protected val mSessionsViewModel = SessionsViewModel()

    inner class MyViewHolder(private val mViewMvc: SessionViewMvc<ListenerType>) :
        RecyclerView.ViewHolder(mViewMvc.rootView!!) {
        val view: SessionViewMvc<ListenerType> get() = mViewMvc

    }

    protected var mSessionUUIDS: MutableList<String> = mutableListOf()
    protected var mSessionPresenters: HashMap<String, SessionPresenter> = hashMapOf()
    lateinit var mItemTouchHelper: ItemTouchHelper

    abstract fun prepareSession(localSession: LocalSession, expanded: Boolean): LocalSession

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val uuid = mSessionUUIDS[position]
        val sessionPresenter = mSessionPresenters[uuid]
        // TODO: Looks like it binds more frequently than needed. This causes a lot of unnecessary recalculation on chart
        sessionPresenter?.let {
            holder.view.bindSession(sessionPresenter)
        }
    }

    override fun getItemCount(): Int {
        return mSessionUUIDS.size
    }

    protected open fun removeObsoleteSessions() {
        mSessionPresenters.keys
            .filter { uuid -> !mSessionUUIDS.contains(uuid) }
            .forEach { uuid ->
                mSessionPresenters.remove(uuid)
            }
    }

    fun bindSessions(localSessions: List<LocalSession>, sensorThresholds: HashMap<String, SensorThreshold>) {
        mSessionUUIDS = localSessions.map { session -> session.uuid }.toMutableList()
        removeObsoleteSessions()
        localSessions.forEach { session ->
            if (mSessionPresenters.containsKey(session.uuid)) {
                val sessionPresenter = mSessionPresenters[session.uuid]
                sessionPresenter!!.localSession = prepareSession(session, sessionPresenter.expanded)
                // TODO: Take conditions that ask about refreshing here
                sessionPresenter.chartData?.refresh(session)
            } else {
                val sessionPresenter = SessionPresenter(session, sensorThresholds)
                mSessionPresenters[session.uuid] = sessionPresenter
            }
        }

        notifyDataSetChanged()
    }

    fun showLoaderFor(localSession: LocalSession) {
        val sessionPresenter = mSessionPresenters[localSession.uuid]
        sessionPresenter?.loading = true

        notifyDataSetChanged()
    }

    fun hideLoaderFor(deviceId: String) {
        val sessionPresenter = mSessionPresenters.values.find { sessionPresenter -> sessionPresenter.localSession?.deviceId == deviceId }
        sessionPresenter?.loading = false

        notifyDataSetChanged()
    }

    fun hideLoaderFor(localSession: LocalSession) {
        val sessionPresenter = mSessionPresenters[localSession.uuid]
        sessionPresenter?.loading = false

        notifyDataSetChanged()
    }

    fun showReconnectingLoaderFor(localSession: LocalSession) {
        val sessionPresenter = mSessionPresenters[localSession.uuid]
        sessionPresenter?.reconnecting = true

        notifyDataSetChanged()
    }

    fun hideReconnectingLoaderFor(localSession: LocalSession) {
        val sessionPresenter = mSessionPresenters[localSession.uuid]

        sessionPresenter?.reconnecting = false
        notifyDataSetChanged()
    }

    fun reloadSession(localSession: LocalSession) {
        val sessionPresenter = mSessionPresenters[localSession.uuid]
        sessionPresenter?.localSession = localSession

        notifyDataSetChanged()
    }

    protected fun reloadSessionFromDB(localSession: LocalSession): LocalSession {
        var reloadedLocalSession: LocalSession? = null

        runBlocking {
            val query = GlobalScope.async(Dispatchers.IO) {
                val dbSessionWithMeasurements = mSessionsViewModel.reloadSessionWithMeasurements(localSession.uuid)
                dbSessionWithMeasurements?.let {
                    reloadedLocalSession = LocalSession(dbSessionWithMeasurements)
                }
            }
            query.await()
        }

        return reloadedLocalSession ?: localSession
    }
}
