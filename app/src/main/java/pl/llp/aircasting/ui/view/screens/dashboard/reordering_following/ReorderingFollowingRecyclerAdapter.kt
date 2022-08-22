package pl.llp.aircasting.ui.view.screens.dashboard.reordering_following

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionCardListener
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.dashboard.following.FollowingRecyclerAdapter
import pl.llp.aircasting.ui.view.screens.dashboard.helpers.SessionFollower
import pl.llp.aircasting.util.ItemTouchHelperAdapter
import pl.llp.aircasting.util.Settings
import java.util.*

class ReorderingFollowingRecyclerAdapter(
    private val mInflater: LayoutInflater,
    private val mListener: SessionCardListener,
    supportFragmentManager: FragmentManager
) : FollowingRecyclerAdapter(mInflater, mListener, supportFragmentManager),
    ItemTouchHelperAdapter {

    private val mApplication: AircastingApplication =
        mInflater.context.applicationContext as AircastingApplication
    private val mSettings = Settings(mApplication)
    private val mSessionRepository = SessionsRepository()
    private val mActiveSessionsRepository = ActiveSessionMeasurementsRepository()

    private var mSessionFollower =
        SessionFollower(mSettings, mActiveSessionsRepository, mSessionRepository)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc =
            ReorderingFollowingSessionViewMvcImpl(mInflater, parent, supportFragmentManager)

        viewMvc.registerListener(mListener)
        val myReorderingViewHolder = MyViewHolder(viewMvc)
        myReorderingViewHolder.itemView.findViewById<ImageView>(R.id.reorder_session_button)
            .setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_DOWN)
                    mItemTouchHelper.startDrag(myReorderingViewHolder)
                true
            }

        return myReorderingViewHolder
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                updateSessionsOrder(i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                updateSessionsOrder(i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun initSessionPresenter(
        session: Session,
        sensorThresholds: HashMap<String, SensorThreshold>
    ): SessionPresenter {
        return SessionPresenter(session, sensorThresholds, expanded = false)
    }

    private fun updateSessionsOrder(firstPosition: Int, secondPosition: Int) {
        Collections.swap(mSessionUUIDS, firstPosition, secondPosition)

        CoroutineScope(Dispatchers.IO).launch {
            mSessionsViewModel.updateOrder(mSessionUUIDS[secondPosition], secondPosition)
            mSessionsViewModel.updateOrder(mSessionUUIDS[firstPosition], firstPosition)
        }
    }

    override fun onItemDismiss(position: Int) {
        mSessionUUIDS.removeAt(position)

        removeObsoleteSessions()
        notifyItemRemoved(position)
    }

    override fun removeObsoleteSessions() {
        mSessionPresenters.keys
            .filter { uuid -> !mSessionUUIDS.contains(uuid) }
            .forEach { uuid ->
                val sessionPresenter = mSessionPresenters[uuid]
                val session = sessionPresenter?.session

                session?.let { mSession -> mSessionFollower.unfollow(mSession) }

                mSessionPresenters.remove(uuid)
            }
    }

}
