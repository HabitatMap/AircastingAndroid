package pl.llp.aircasting.ui.view.screens.dashboard

import android.view.LayoutInflater
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import kotlinx.coroutines.launch
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.observers.SessionsObserver
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.extensions.found
import pl.llp.aircasting.util.extensions.startAnimation
import pl.llp.aircasting.util.extensions.stopAnimation

abstract class SessionsRecyclerAdapter<ListenerType>(
    private val recyclerView: RecyclerView?,
    private val mInflater: LayoutInflater,
    protected val supportFragmentManager: FragmentManager,
    protected val mSessionsViewModel: SessionsViewModel = SessionsViewModel()
) : RecyclerView.Adapter<SessionsRecyclerAdapter<ListenerType>.MyViewHolder>() {

    inner class MyViewHolder(private val mViewMvc: SessionViewMvc<ListenerType>) :
        RecyclerView.ViewHolder(mViewMvc.rootView!!) {
        val view: SessionViewMvc<ListenerType> get() = mViewMvc
    }

    lateinit var mItemTouchHelper: ItemTouchHelper

    protected open val mSessionPresenters: SortedList<SessionPresenter> =
        SortedList(SessionPresenter::class.java, ModificationCallback())

    abstract suspend fun prepareSession(session: Session, expanded: Boolean): Session

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
                mSessionsViewModel.viewModelScope.launch {
                    presenter.session =
                        prepareSession(it, mSessionPresenters[position].expanded)

                    presenter.chartData?.refresh(it)

                    mSessionPresenters.updateItemAt(position, presenter)
                }
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

    protected open fun initSessionPresenter(
        session: Session,
        sensorThresholds: Map<String, SensorThreshold>
    ) = SessionPresenter(session, sensorThresholds)

    fun toggleLoaderFor(session: Session, loading: Boolean) {
        val position = mSessionPresenters.indexOf(SessionPresenter(session))
        if (found(position)) {
            mSessionPresenters[position]?.loading = loading

            toggleLoaderAt(position, loading)
        }
    }

    private fun toggleLoaderAt(position: Int, loading: Boolean) {
        val view = recyclerView?.layoutManager?.findViewByPosition(position)
        val loader = view?.findViewById<ImageView>(R.id.loader)
        if (loading)
            loader?.startAnimation()
        else
            loader?.stopAnimation()
    }

    fun toggleReconnectingLoaderFor(
        session: Session,
        reconnecting: Boolean
    ) {
        val position = mSessionPresenters.indexOf(SessionPresenter(session))
        if (found(position)) {
            val sessionPresenter = mSessionPresenters[position]
            sessionPresenter?.reconnecting = reconnecting

            toggleLoaderAt(position, reconnecting)
        }
    }

    fun hideLoaderFor(deviceId: String) {
        val position = indexOfPresenter(deviceId)
        if (found(position)) {
            mSessionPresenters[position]?.loading = false

            toggleLoaderAt(position, false)
        }
    }

    private fun indexOfPresenter(deviceId: String): Int {
        for (i in 0..mSessionPresenters.size()) {
            if (mSessionPresenters[i].session?.deviceId == deviceId)
                return i
        }
        return -1
    }

    open fun reloadSession(session: Session) {
        update(session)
    }

    protected suspend fun reloadSessionFromDB(session: Session): Session {
        val reloadedSession: Session? = getFromDB(session)

        return reloadedSession ?: session
    }

    private suspend fun getFromDB(session: Session): Session? {
        val dbSessionWithMeasurements =
            mSessionsViewModel.reloadSessionWithMeasurementsSuspend(session.uuid)

        return dbSessionWithMeasurements?.let { Session(it) }
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
