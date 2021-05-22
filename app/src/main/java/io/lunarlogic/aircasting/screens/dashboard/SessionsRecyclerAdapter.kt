package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.models.SensorThreshold
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel


abstract class SessionsRecyclerAdapter<ListenerType>(
    private val mInflater: LayoutInflater,
    protected val supportFragmentManager: FragmentManager
): RecyclerView.Adapter<SessionsRecyclerAdapter<ListenerType>.MyViewHolder>() {
    private val mSessionsViewModel = SessionsViewModel()

    inner class MyViewHolder(private val mViewMvc: SessionViewMvc<ListenerType>) :
        RecyclerView.ViewHolder(mViewMvc.rootView!!) {
        val view: SessionViewMvc<ListenerType> get() = mViewMvc
    }

    private var mSessionUUIDS: List<String> = emptyList()
    private var mSessionPresenters: HashMap<String, SessionPresenter> = hashMapOf()

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val uuid = mSessionUUIDS.get(position)
        val sessionPresenter = mSessionPresenters[uuid]
        sessionPresenter?.let {
            holder.view.bindSession(sessionPresenter)
        }
    }

    override fun getItemCount(): Int {
        return mSessionPresenters.size
    }

    private fun removeObsoleteSessions() {
        mSessionPresenters.keys
            .filter { uuid -> !mSessionUUIDS.contains(uuid) }
            .forEach { uuid -> mSessionPresenters.remove(uuid) }
    }

    fun bindSessions(sessions: List<Session>, sensorThresholds: HashMap<String, SensorThreshold>) {
        mSessionUUIDS = sessions.map { session -> session.uuid }
        removeObsoleteSessions()
        sessions.forEach { session ->
            if (mSessionPresenters.containsKey(session.uuid)) {
                val sessionPresenter = mSessionPresenters[session.uuid]
                if (sessionPresenter?.expanded == true && sessionPresenter.session?.tab == SessionsTab.FIXED) {
                     downloadMeasurementsForSession(sessionPresenter.session!!)
                }
                sessionPresenter!!.session = session
                sessionPresenter!!.chartData?.refresh(session)
            } else {
                val sessionPresenter = SessionPresenter(session, sensorThresholds)
                mSessionPresenters[session.uuid] = sessionPresenter
            }
        }

        notifyDataSetChanged()
    }

    fun downloadMeasurementsForSession(session: Session) {
        showLoaderFor(session)
        reloadSessionMeasurements(session)
    }

    protected fun reloadSessionMeasurements(session: Session) {
        DatabaseProvider.runQuery { scope ->
            val dbSessionWithMeasurements = mSessionsViewModel.reloadSessionWithMeasurements(session.uuid)
            dbSessionWithMeasurements?.let {
                val reloadedSession = Session(dbSessionWithMeasurements)

                DatabaseProvider.backToUIThread(scope) {
                    reloadSession(reloadedSession)
                    hideLoaderFor(session)
                }
            }
        }
    }

    fun showLoaderFor(session: Session) {
        val sessionPresenter = mSessionPresenters[session.uuid]
        sessionPresenter?.loading = true

        notifyDataSetChanged()
    }

    fun hideLoaderFor(deviceId: String) {
        val sessionPresenter = mSessionPresenters.values.find { sessionPresenter -> sessionPresenter.session?.deviceId == deviceId }
        sessionPresenter?.loading = false

        notifyDataSetChanged()
    }

    fun hideLoaderFor(session: Session) {
        val sessionPresenter = mSessionPresenters[session.uuid]
        sessionPresenter?.loading = false

        notifyDataSetChanged()
    }

    fun showReconnectingLoaderFor(session: Session) {
        val sessionPresenter = mSessionPresenters[session.uuid]
        sessionPresenter?.reconnecting = true

        notifyDataSetChanged()
    }

    fun hideReconnectingLoaderFor(session: Session) {
        val sessionPresenter = mSessionPresenters[session.uuid]
        sessionPresenter?.reconnecting = false

        notifyDataSetChanged()
    }

    fun reloadSession(session: Session) {
        val sessionPresenter = mSessionPresenters[session.uuid]
        sessionPresenter?.session = session

        notifyDataSetChanged()
    }
}
