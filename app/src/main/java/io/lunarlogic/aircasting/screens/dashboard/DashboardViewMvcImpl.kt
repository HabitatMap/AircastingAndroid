package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.Session

class DashboardViewMvcImpl : BaseObservableViewMvc<DashboardViewMvc.Listener>, DashboardViewMvc, SessionViewMvc.Listener {
    private var mRecordSessionButton: Button? = null

    private var mRecyclerSessions: RecyclerView? = null
    private var mEmptyView: View? = null
    private val mAdapter: SessionsRecyclerAdapter

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.fragment_dashboard, parent, false)

        mEmptyView = rootView?.findViewById(R.id.empty_dashboard)
        mRecordSessionButton = rootView?.findViewById(R.id.dashboard_record_new_session_button)
        mRecordSessionButton?.setOnClickListener {
            onRecordNewSessionClicked()
        }

        mRecyclerSessions = findViewById(R.id.recycler_sessions)
        mRecyclerSessions?.setLayoutManager(LinearLayoutManager(rootView!!.context))
        mAdapter = SessionsRecyclerAdapter(
            inflater,
            this
        )
        mRecyclerSessions?.setAdapter(mAdapter)
    }

    private fun onRecordNewSessionClicked() {
        for (listener in listeners) {
            listener.onRecordNewSessionClicked()
        }
    }

    override fun showSessionsView(sessions: List<Session>) {
        mAdapter.bindSessions(sessions)
        mRecyclerSessions?.visibility = View.VISIBLE
        mEmptyView?.visibility = View.INVISIBLE
    }

    override fun showEmptyView() {
        mEmptyView?.visibility = View.VISIBLE
        mRecyclerSessions?.visibility = View.INVISIBLE
    }

    override fun onSessionStopClicked(session: Session) {
        for (listener in listeners) {
            listener.onStopSessionClicked(session.uuid)
        }
    }
}