package pl.llp.aircasting.ui.view.screens.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import pl.llp.aircasting.R
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.observers.SessionsObserver
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc

abstract class SessionsViewMvcImpl<ListenerType>(
    private val inflater: LayoutInflater,
    parent: ViewGroup?,
    private val supportFragmentManager: FragmentManager,
) : BaseObservableViewMvc<SessionsViewMvc.Listener>(),
    SessionsViewMvc {
    protected lateinit var reloadSession: suspend (uuid: String) -> SessionWithStreamsAndMeasurementsDBObject?
    private var mRecordSessionButton: Button? = null
    private var mOnExploreBtn: Button? = null
    protected var mRecyclerSessions: RecyclerView? = null
    private var mLoading: TextView? = null
    private var mEmptyView: View? = null
    private lateinit var mAdapter: SessionsRecyclerAdapter<ListenerType>
    var mDidYouKnowBox: MaterialCardView? = null

    init {
        this.rootView = inflater.inflate(R.layout.fragment_sessions_tab, parent, false)
        mEmptyView = findViewById(layoutId())
        mRecordSessionButton = findViewById(recordNewSessionButtonId())
        mOnExploreBtn = findViewById(onExploreNewSessionsButtonID())
        mDidYouKnowBox = findViewById(R.id.did_you_know_box)
        mRecyclerSessions = findViewById(R.id.recycler_sessions)
        mLoading = findViewById(R.id.loading)
        mRecyclerSessions?.itemAnimator = null

        mRecordSessionButton?.setOnClickListener { onRecordNewSessionClicked() }
        mOnExploreBtn?.setOnClickListener { onExploreNewSessionsClicked() }

        mRecyclerSessions?.layoutManager = LinearLayoutManager(rootView?.context)
        mDidYouKnowBox?.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.navigation_lets_begin)
        }
    }

    open fun initializeAdapter(callback: suspend (String) -> SessionWithStreamsAndMeasurementsDBObject?) {
        reloadSession = callback
        mAdapter = buildAdapter(inflater, supportFragmentManager)
        mRecyclerSessions?.adapter = mAdapter
    }
    abstract fun layoutId(): Int
    abstract fun showDidYouKnowBox(): Boolean
    abstract fun recordNewSessionButtonId(): Int
    abstract fun onExploreNewSessionsButtonID(): Int

    abstract fun buildAdapter(
        inflater: LayoutInflater,
        supportFragmentManager: FragmentManager,
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

    override fun showSessionsView(
        modifiedSessions: Map<SessionsObserver.ModificationType, List<Session>>,
        sensorThresholds: Map<String, SensorThreshold>
    ) {
        if (recyclerViewCanBeUpdated()) {
            mAdapter.bindSessions(modifiedSessions, sensorThresholds)
            mEmptyView?.visibility = View.INVISIBLE
            mDidYouKnowBox?.visibility = View.INVISIBLE
            mRecyclerSessions?.visibility = View.VISIBLE
            // hide loader
            mLoading?.visibility = View.INVISIBLE
        }
    }

    override fun showEmptyView() {
        mEmptyView?.visibility = View.VISIBLE
        mRecyclerSessions?.visibility = View.INVISIBLE
        mLoading?.visibility = View.INVISIBLE
        if (showDidYouKnowBox()) {
            mDidYouKnowBox?.visibility = View.VISIBLE
        } else {
            mDidYouKnowBox?.visibility = View.INVISIBLE
        }
    }

    override fun showLoaderFor(session: Session) {
        mAdapter.toggleLoaderFor(session, true)
    }

    override fun hideLoaderFor(session: Session) {
        mAdapter.toggleLoaderFor(session, false)
    }

    override fun hideLoaderFor(deviceId: String) {
        mAdapter.hideLoaderFor(deviceId)
    }

    override fun reloadSession(session: Session) {
        mAdapter.reloadSession(session)
    }

    private fun recyclerViewCanBeUpdated(): Boolean {
        return mRecyclerSessions?.isComputingLayout == false && mRecyclerSessions?.scrollState == RecyclerView.SCROLL_STATE_IDLE
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
