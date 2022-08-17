package pl.llp.aircasting.ui.view.screens.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.observers.SessionsObserver
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc
import pl.llp.aircasting.util.FollowingSessionReorderingTouchHelperCallback
import pl.llp.aircasting.util.ItemTouchHelperAdapter

abstract class SessionsViewMvcImpl<ListenerType>(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
) : BaseObservableViewMvc<SessionsViewMvc.Listener>(), SessionsViewMvc {
    private var mRecordSessionButton: Button? = null
    private var mOnExploreBtn: Button? = null
    protected var mRecyclerSessions: RecyclerView? = null
    private var mEmptyView: View? = null
    protected val mAdapter: SessionsRecyclerAdapter<ListenerType>
    var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    var mDidYouKnowBox: MaterialCardView? = null

    init {
        this.rootView = inflater.inflate(R.layout.fragment_sessions_tab, parent, false)
        mEmptyView = findViewById(layoutId())
        mRecordSessionButton = findViewById(recordNewSessionButtonId())
        mOnExploreBtn = findViewById(onExploreNewSessionsButtonID())
        mDidYouKnowBox = findViewById(R.id.did_you_know_box)
        mRecyclerSessions = findViewById(R.id.recycler_sessions)
        mRecyclerSessions?.itemAnimator = null

        mRecordSessionButton?.setOnClickListener { onRecordNewSessionClicked() }
        mOnExploreBtn?.setOnClickListener { onExploreNewSessionsClicked() }

        mRecyclerSessions?.layoutManager = LinearLayoutManager(rootView?.context)
        mDidYouKnowBox?.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.navigation_lets_begin)
        }
        mAdapter = buildAdapter(inflater, supportFragmentManager)
        mRecyclerSessions?.adapter = mAdapter
        addTouchHelperToRecyclerView()
        if (mAdapter is ItemTouchHelperAdapter) {
            val itemTouchCallback = FollowingSessionReorderingTouchHelperCallback(mAdapter)
            val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
            itemTouchHelper.attachToRecyclerView(mRecyclerSessions)
        }
        setupSwipeToRefreshLayout()
    }

    abstract fun layoutId(): Int
    abstract fun showDidYouKnowBox(): Boolean
    abstract fun recordNewSessionButtonId(): Int
    abstract fun onExploreNewSessionsButtonID(): Int
    abstract fun addTouchHelperToRecyclerView()

    abstract fun buildAdapter(
        inflater: LayoutInflater,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<ListenerType>

    private fun onRecordNewSessionClicked() {
        for (listener in listeners) {
            listener.onRecordNewSessionClicked()
        }
    }

    private fun onExploreNewSessionsClicked() {
        for (listener in listeners) {
            listener.onExploreNewSessionsClicked()
        }
    }

    private fun onSwipeToRefreshTriggered() {
        for (listener in listeners) {
            listener.onSwipeToRefreshTriggered()
        }
    }

    override fun showSessionsView(
        modifiedSessions: Map<SessionsObserver.ModificationType, List<Session>>,
        sensorThresholds: HashMap<String, SensorThreshold>
    ) {
        if (recyclerViewCanBeUpdated()) {
            mAdapter.bindSessions(modifiedSessions, sensorThresholds)
            mEmptyView?.visibility = View.INVISIBLE
            mDidYouKnowBox?.visibility = View.INVISIBLE
            mRecyclerSessions?.visibility = View.VISIBLE
        }
    }

    override fun showEmptyView() {
        mEmptyView?.visibility = View.VISIBLE
        mRecyclerSessions?.visibility = View.INVISIBLE
        if (showDidYouKnowBox()) {
            mDidYouKnowBox?.visibility = View.VISIBLE
        } else {
            mDidYouKnowBox?.visibility = View.INVISIBLE
        }
    }

    override fun showLoaderFor(session: Session) {
        mAdapter.showLoaderFor(session)
    }

    override fun hideLoaderFor(session: Session) {
        mAdapter.hideLoaderFor(session)
    }

    override fun hideLoaderFor(deviceId: String) {
        mAdapter.hideLoaderFor(deviceId)
    }

    override fun showReconnectingLoaderFor(session: Session) {
        mAdapter.showReconnectingLoaderFor(session)
    }

    override fun hideReconnectingLoaderFor(session: Session) {
        mAdapter.hideReconnectingLoaderFor(session)
    }

    override fun reloadSession(session: Session) {
        mAdapter.reloadSession(session)
    }

    override fun showLoader() {
        //mSwipeRefreshLayout?.isRefreshing = true
    }

    override fun hideLoader() {
        mSwipeRefreshLayout?.isRefreshing = false
    }

    private fun recyclerViewCanBeUpdated(): Boolean {
        return mRecyclerSessions?.isComputingLayout == false && mRecyclerSessions?.scrollState == RecyclerView.SCROLL_STATE_IDLE
    }

    private fun setupSwipeToRefreshLayout() {
        mSwipeRefreshLayout = rootView?.findViewById(R.id.refresh_sessions)
        mSwipeRefreshLayout?.let { layout ->
            layout.setColorSchemeResources(R.color.aircasting_blue_400)
            layout.setOnRefreshListener {
                onSwipeToRefreshTriggered()
            }
        }
    }

    fun onExpandSessionCard(session: Session) {
        for (listener in listeners) {
            listener.onExpandSessionCard(session)
        }
    }

    fun onCollapseSessionCard(session: Session) {
        for (listener in listeners) {
            listener.onCollapseSessionCard(session)
        }
    }

    fun onFollowButtonClicked(session: Session) {
        for (listener in listeners) {
            listener.onFollowButtonClicked(session)
        }
    }

    fun onUnfollowButtonClicked(session: Session) {
        for (listener in listeners) {
            listener.onUnfollowButtonClicked(session)
        }
    }

    fun onMapButtonClicked(session: Session, measurementStream: MeasurementStream?) {
        for (listener in listeners) {
            listener.onMapButtonClicked(session, measurementStream?.sensorName)
        }
    }

    fun onGraphButtonClicked(session: Session, measurementStream: MeasurementStream?) {
        for (listener in listeners) {
            listener.onGraphButtonClicked(session, measurementStream?.sensorName)
        }
    }
}
