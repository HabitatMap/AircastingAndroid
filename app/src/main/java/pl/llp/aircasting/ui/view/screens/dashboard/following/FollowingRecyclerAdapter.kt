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
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.extensions.expandedCards

open class FollowingRecyclerAdapter(
    private val mInflater: LayoutInflater,
    private val mListener: SessionCardListener,
    supportFragmentManager: FragmentManager,
    sessionsViewModel: SessionsViewModel = SessionsViewModel()
) : SessionsRecyclerAdapter<SessionCardListener>(mInflater, supportFragmentManager, sessionsViewModel) {
    override val mSessionPresenters: SortedList<SessionPresenter> =
        SortedList(SessionPresenter::class.java, FollowingModificationCallback())

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
        override fun compare(first: SessionPresenter?, second: SessionPresenter?): Int {
            val firstFollowedAt = first?.session?.followedAt
            val secondFollowedAt = second?.session?.followedAt
            return if (firstFollowedAt != null && secondFollowedAt != null)
                secondFollowedAt.compareTo(firstFollowedAt)
            else 0
        }
    }
}
