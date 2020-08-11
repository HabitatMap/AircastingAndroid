package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.Session

abstract class SessionsViewMvcImpl<ListenerType>: BaseObservableViewMvc<SessionsViewMvc.Listener>, SessionsViewMvc {
    private var mRecordSessionButton: Button? = null

    private var mRecyclerSessions: RecyclerView? = null
    private var mEmptyView: View? = null
    private val mAdapter: SessionsRecyclerAdapter<ListenerType>

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_sessions_tab, parent, false)

        mEmptyView = rootView?.findViewById(R.id.empty_dashboard)
        mRecordSessionButton = rootView?.findViewById(R.id.dashboard_record_new_session_button)
        mRecordSessionButton?.setOnClickListener {
            onRecordNewSessionClicked()
        }

        mRecyclerSessions = findViewById(R.id.recycler_sessions)
        mRecyclerSessions?.setLayoutManager(LinearLayoutManager(rootView!!.context))
        mAdapter = buildAdapter(inflater, supportFragmentManager)
        mRecyclerSessions?.setAdapter(mAdapter)

        val swipeRefreshLayout = rootView?.findViewById<SwipeRefreshLayout>(R.id.refresh_sessions)
        swipeRefreshLayout?.setOnRefreshListener {
            val callback = { swipeRefreshLayout.isRefreshing = false }
            onSwipeToRefreshTriggered(callback)
        }
    }

    abstract fun buildAdapter(
        inflater: LayoutInflater,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<ListenerType>

    private fun onRecordNewSessionClicked() {
        for (listener in listeners) {
            listener.onRecordNewSessionClicked()
        }
    }

    private fun onSwipeToRefreshTriggered(callback: () -> Unit) {
        for (listener in listeners) {
            listener.onSwipeToRefreshTriggered(callback)
        }
    }

    override fun showSessionsView(sessions: List<Session>) {
        if (recyclerViewCanBeUpdated()) {
            mAdapter.bindSessions(sessions)
            mRecyclerSessions?.visibility = View.VISIBLE
            mEmptyView?.visibility = View.INVISIBLE
        }
    }

    override fun showEmptyView() {
        mEmptyView?.visibility = View.VISIBLE
        mRecyclerSessions?.visibility = View.INVISIBLE
    }

    private fun recyclerViewCanBeUpdated(): Boolean {
        return mRecyclerSessions?.isComputingLayout == false
                && mRecyclerSessions?.scrollState == RecyclerView.SCROLL_STATE_IDLE
    }
}
