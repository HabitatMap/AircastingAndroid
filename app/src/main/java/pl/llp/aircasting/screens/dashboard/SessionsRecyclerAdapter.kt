package pl.llp.aircasting.screens.dashboard

import android.view.LayoutInflater
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import pl.llp.aircasting.models.SensorThreshold
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.models.SessionsViewModel


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
    abstract fun prepareSession(session: Session, expanded: Boolean): Session

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val uuid = mSessionUUIDS.get(position)
        val sessionPresenter = mSessionPresenters[uuid]
        sessionPresenter?.let {
            holder.view.bindSession(sessionPresenter)
        }
    }

    override fun getItemCount(): Int {
        return mSessionUUIDS.size // todo: changed from mSessionPresenters
    }

    protected open fun removeObsoleteSessions() {
        mSessionPresenters.keys
            .filter { uuid -> !mSessionUUIDS.contains(uuid) }
            .forEach { uuid ->
                mSessionPresenters.remove(uuid)
            }
    }

    fun bindSessions(sessions: List<Session>, sensorThresholds: HashMap<String, SensorThreshold>) {
        mSessionUUIDS = sessions.map { session -> session.uuid }.toMutableList()
        removeObsoleteSessions()
        sessions.forEach { session ->
            if (mSessionPresenters.containsKey(session.uuid)) {
                val sessionPresenter = mSessionPresenters[session.uuid]
                sessionPresenter!!.session = prepareSession(session, sessionPresenter.expanded)
                sessionPresenter.chartData?.refresh(session)
            } else {
                val sessionPresenter = SessionPresenter(session, sensorThresholds)
                mSessionPresenters[session.uuid] = sessionPresenter
            }
        }

        notifyDataSetChanged()
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
