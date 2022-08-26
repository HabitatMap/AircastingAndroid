package pl.llp.aircasting.ui.view.screens.dashboard

import android.view.LayoutInflater
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.observers.SessionsObserver
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel

abstract class SessionsRecyclerAdapter<ListenerType>(
    private val mInflater: LayoutInflater,
    protected val supportFragmentManager: FragmentManager
) : RecyclerView.Adapter<SessionsRecyclerAdapter<ListenerType>.MyViewHolder>() {
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

    fun bindSessions(
        modifiedSessions: Map<SessionsObserver.ModificationType, List<Session>>,
        sensorThresholds: Map<String, SensorThreshold>
    ) {
        delete(modifiedSessions[SessionsObserver.ModificationType.DELETED])
        update(modifiedSessions[SessionsObserver.ModificationType.UPDATED])
        insert(modifiedSessions[SessionsObserver.ModificationType.INSERTED], sensorThresholds)
    }

    private fun delete(sessions: List<Session>?) {
        sessions?.forEach { session ->
            val position = mSessionUUIDS.indexOf(session.uuid)
            if (found(position)) {
                mSessionUUIDS.removeAt(position)
                mSessionPresenters.remove(session.uuid)
                notifyItemRemoved(position)
            }
        }
    }

    private fun update(sessions: List<Session>?) {
        sessions?.forEach { session ->
            val position = mSessionUUIDS.indexOf(session.uuid)
            if (found(position)) {
                replaceSession(position, session)

                val success = replacePresenter(session)
                if (success) notifyItemChanged(position)
            }
        }
    }

    private fun insert(
        sessions: List<Session>?,
        sensorThresholds: Map<String, SensorThreshold>
    ) {
        sessions?.forEach { session ->
            val position = mSessionUUIDS.indexOf(session.uuid)
            if (!found(position)) {
                mSessionUUIDS.add(session.uuid)

                val sessionPresenter = initSessionPresenter(session, sensorThresholds)
                mSessionPresenters[session.uuid] = sessionPresenter

                notifyItemInserted(mSessionUUIDS.lastIndex)
            }
        }
    }

    private fun replacePresenter(session: Session): Boolean {
        val sessionPresenter = mSessionPresenters[session.uuid]
        if (sessionPresenter != null) {
            sessionPresenter.session = prepareSession(session, sessionPresenter.expanded)
            sessionPresenter.chartData?.refresh(session)
            return true
        }
        return false
    }

    private fun replaceSession(position: Int, session: Session) {
        mSessionUUIDS[position] = session.uuid
    }

    private fun found(position: Int) = position != -1

    protected open fun initSessionPresenter(
        session: Session,
        sensorThresholds: Map<String, SensorThreshold>
    ) = SessionPresenter(
        session,
        sensorThresholds
    )

    fun showLoaderFor(session: Session) {
        val sessionPresenter = mSessionPresenters[session.uuid]
        sessionPresenter?.loading = true

        notifyItemChanged(mSessionUUIDS.indexOf(session.uuid))
    }

    fun hideLoaderFor(deviceId: String) {
        val sessionPresenter =
            mSessionPresenters.values.find { sessionPresenter -> sessionPresenter.session?.deviceId == deviceId }
        sessionPresenter?.loading = false

        notifyItemChanged(mSessionUUIDS.indexOf(sessionPresenter?.session?.uuid))
    }

    fun hideLoaderFor(session: Session) {
        val sessionPresenter = mSessionPresenters[session.uuid]
        sessionPresenter?.loading = false

        notifyItemChanged(mSessionUUIDS.indexOf(session.uuid))
    }

    fun showReconnectingLoaderFor(session: Session) {
        val sessionPresenter = mSessionPresenters[session.uuid]
        sessionPresenter?.reconnecting = true

        notifyItemChanged(mSessionUUIDS.indexOf(session.uuid))
    }

    fun hideReconnectingLoaderFor(session: Session) {
        val sessionPresenter = mSessionPresenters[session.uuid]

        sessionPresenter?.reconnecting = false
        notifyItemChanged(mSessionUUIDS.indexOf(session.uuid))
    }

    fun reloadSession(session: Session) {
        val sessionPresenter = mSessionPresenters[session.uuid]
        sessionPresenter?.session = session
    }

    protected fun reloadSessionFromDB(session: Session): Session {
        val reloadedSession: Session? = getFromDB(session)

        return reloadedSession ?: session
    }

    private fun getFromDB(session: Session): Session? = runBlocking {
        withContext(Dispatchers.IO) {
            val dbSessionWithMeasurements =
                mSessionsViewModel.reloadSessionWithMeasurements(session.uuid)
            return@withContext dbSessionWithMeasurements?.let { Session(it) }
        }
    }
}
