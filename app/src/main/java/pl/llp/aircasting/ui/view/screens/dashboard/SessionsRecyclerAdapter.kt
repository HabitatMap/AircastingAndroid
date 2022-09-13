package pl.llp.aircasting.ui.view.screens.dashboard

import android.view.LayoutInflater
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.observers.SessionsObserver
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.extensions.runOnIOThread

abstract class SessionsRecyclerAdapter<ListenerType>(
    private val mInflater: LayoutInflater,
    protected val supportFragmentManager: FragmentManager
) : RecyclerView.Adapter<SessionsRecyclerAdapter<ListenerType>.MyViewHolder>() {

    inner class MyViewHolder(private val mViewMvc: SessionViewMvc<ListenerType>) :
        RecyclerView.ViewHolder(mViewMvc.rootView!!) {
        val view: SessionViewMvc<ListenerType> get() = mViewMvc
    }

    protected val mSessionsViewModel = SessionsViewModel()
    lateinit var mItemTouchHelper: ItemTouchHelper

    private val modificationCallback = ModificationCallback()
    protected open val mSessionPresenters: SortedList<SessionPresenter> =
        SortedList(SessionPresenter::class.java, modificationCallback)

    abstract fun prepareSession(session: Session, expanded: Boolean): Session

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val sessionPresenter = mSessionPresenters[position]
        sessionPresenter?.let {
            holder.view.bindSession(sessionPresenter)
        }
    }

    override fun getItemCount() = mSessionPresenters.size()

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
            mSessionPresenters.remove(SessionPresenter(session))
        }
    }

    private fun update(sessions: List<Session>?) {
        sessions?.forEach { session ->
            update(session)
        }
    }

    private fun update(session: Session?) {
        session?.let { it ->
            val position = mSessionPresenters.indexOf(SessionPresenter(it))
            if (found(position)) {
                val presenter = mSessionPresenters[position]
                presenter.session = prepareSession(it, mSessionPresenters[position].expanded)
                presenter.setStream()
                presenter.chartData?.refresh(it)

                mSessionPresenters.updateItemAt(position, presenter)
            }
        }
    }

    private fun insert(
        sessions: List<Session>?,
        sensorThresholds: Map<String, SensorThreshold>
    ) {
        sessions?.forEach { session ->
            val sessionPresenter = initSessionPresenter(session, sensorThresholds)
            mSessionPresenters.add(sessionPresenter)
        }
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
        val position = mSessionPresenters.indexOf(SessionPresenter(session))
        if (found(position)) {
            val sessionPresenter = mSessionPresenters[position]
            sessionPresenter?.loading = true

            notifyItemChanged(position)
        }
    }

    fun hideLoaderFor(session: Session) {
        val position = mSessionPresenters.indexOf(SessionPresenter(session))
        if (found(position)) {
            mSessionPresenters[position]?.loading = false

            notifyItemChanged(position)
        }
    }

    fun showReconnectingLoaderFor(session: Session) {
        val position = mSessionPresenters.indexOf(SessionPresenter(session))
        if (found(position)) {
            val sessionPresenter = mSessionPresenters[position]
            sessionPresenter?.reconnecting = true

            notifyItemChanged(position)
        }
    }

    fun hideReconnectingLoaderFor(session: Session) {
        val position = mSessionPresenters.indexOf(SessionPresenter(session))
        if (found(position)) {
            val sessionPresenter = mSessionPresenters[position]
            sessionPresenter?.reconnecting = false

            notifyItemChanged(position)
        }
    }

    fun hideLoaderFor(deviceId: String) {
        val position = indexOfPresenter(deviceId)
        if (found(position)) {
            mSessionPresenters[position]?.loading = false

            notifyItemChanged(position)
        }
    }

    private fun indexOfPresenter(deviceId: String): Int {
        for (i in 0..mSessionPresenters.size()) {
            if (mSessionPresenters[i].session?.deviceId == deviceId)
                return i
        }
        return -1
    }

    fun reloadSession(session: Session) {
        update(session)
    }

    protected fun reloadSessionFromDB(session: Session): Session {
        val reloadedSession: Session? = getFromDB(session)

        return reloadedSession ?: session
    }

    private fun getFromDB(session: Session): Session? {
        var reloadedSession: Session? = null
        runOnIOThread {
            val dbSessionWithMeasurements =
                mSessionsViewModel.reloadSessionWithMeasurements(session.uuid)
            reloadedSession = dbSessionWithMeasurements?.let { Session(it) }
        }
        return reloadedSession
    }


    open inner class ModificationCallback : SortedList.Callback<SessionPresenter>() {
        override fun compare(first: SessionPresenter?, second: SessionPresenter?) =
            second?.session?.startTime?.compareTo(first?.session?.startTime) ?: 0

        override fun onInserted(position: Int, count: Int) {
            notifyItemInserted(position)
        }

        override fun onRemoved(position: Int, count: Int) {
            notifyItemRemoved(position)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int) {
            notifyItemChanged(position)
        }

        override fun areContentsTheSame(old: SessionPresenter?, new: SessionPresenter?) =
            new?.session?.hasChangedFrom(old?.session) == false


        override fun areItemsTheSame(old: SessionPresenter?, new: SessionPresenter?) =
            old?.session?.uuid == new?.session?.uuid
    }
}
