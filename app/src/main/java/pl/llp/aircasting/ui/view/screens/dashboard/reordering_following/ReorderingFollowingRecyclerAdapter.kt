package pl.llp.aircasting.ui.view.screens.dashboard.reordering_following

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
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

class ReorderingFollowingRecyclerAdapter(
    private val mInflater: LayoutInflater,
    private val mListener: SessionCardListener,
    supportFragmentManager: FragmentManager
) : FollowingRecyclerAdapter(mInflater, mListener, supportFragmentManager),
    ItemTouchHelperAdapter {

    private val mApplication: AircastingApplication =
        mInflater.context.applicationContext as AircastingApplication
    private val mSettings = mApplication.settings
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
        updateSessionsOrder(fromPosition, toPosition)
    }

    override fun initSessionPresenter(
        session: Session,
        sensorThresholds: Map<String, SensorThreshold>
    ): SessionPresenter {
        return SessionPresenter(session, sensorThresholds, expanded = false)
    }

    private fun updateSessionsOrder(firstPosition: Int, secondPosition: Int) {
        val firstPresenter = mSessionPresenters[firstPosition]
        val secondPresenter = mSessionPresenters[secondPosition]

        swapPresentersInAdapterDataset(
            firstPresenter,
            firstPosition,
            secondPresenter,
            secondPosition
        )

        updateSessionsOrderInDatabase(firstPresenter, secondPresenter)
    }

    private fun swapPresentersInAdapterDataset(
        firstPresenter: SessionPresenter,
        firstPosition: Int,
        secondPresenter: SessionPresenter,
        secondPosition: Int
    ) {
        firstPresenter.session?.order = secondPosition
        secondPresenter.session?.order = firstPosition

        mSessionPresenters.recalculatePositionOfItemAt(firstPosition)
        mSessionPresenters.recalculatePositionOfItemAt(secondPosition)
    }

    private fun updateSessionsOrderInDatabase(
        firstPresenter: SessionPresenter,
        secondPresenter: SessionPresenter
    ) {
        val firstPresenterUUID = firstPresenter.session?.uuid.toString()
        val secondPresenterUUID = secondPresenter.session?.uuid.toString()

        mSessionsViewModel.updateOrder(
            firstPresenterUUID, firstPresenter.session?.order ?: 0
        )
        mSessionsViewModel.updateOrder(
            secondPresenterUUID, secondPresenter.session?.order ?: 0
        )
    }

    override fun onItemDismiss(position: Int) {
        mSessionPresenters.removeItemAt(position)

        val session = mSessionPresenters[position].session ?: return
        mSessionFollower.unfollow(session)
    }
}
