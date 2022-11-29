package pl.llp.aircasting.ui.view.screens.dashboard.following

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.SortedList
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionCardListener
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsRecyclerAdapter
import pl.llp.aircasting.util.extensions.expandedCards

open class FollowingRecyclerAdapter(
    private val mInflater: LayoutInflater,
    private val mListener: SessionCardListener,
    supportFragmentManager: FragmentManager
) : SessionsRecyclerAdapter<SessionCardListener>(mInflater, supportFragmentManager) {
    private val followingModificationCallback: ModificationCallback =
        FollowingModificationCallback()
    override val mSessionPresenters: SortedList<SessionPresenter> =
        SortedList(SessionPresenter::class.java, followingModificationCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc =
            FollowingSessionViewMvcImpl(
                mInflater,
                parent,
                supportFragmentManager
            )
        viewMvc.registerListener(mListener)
        return MyViewHolder(viewMvc)
    }

    override fun initSessionPresenter(
        session: Session,
        sensorThresholds: Map<String, SensorThreshold>
    ): SessionPresenter {
        val expandedState = expandedCards()?.contains(session.uuid) ?: false
        return SessionPresenter(session, sensorThresholds, expanded = expandedState)
    }

    override suspend fun prepareSession(session: Session, expanded: Boolean) =
        reloadSessionFromDB(session)

    inner class FollowingModificationCallback : ModificationCallback() {
        override fun compare(first: SessionPresenter?, second: SessionPresenter?) =
            first?.session?.order?.compareTo(second?.session?.order ?: 0) ?: 0
    }
}
