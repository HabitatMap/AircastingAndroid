package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import android.view.LayoutInflater
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import io.lunarlogic.aircasting.sensor.Session


abstract class SessionsRecyclerAdapter<ListenerType>(
    private val mInflater: LayoutInflater,
    protected val supportFragmentManager: FragmentManager
): RecyclerView.Adapter<SessionsRecyclerAdapter<ListenerType>.MyViewHolder>() {

    inner class MyViewHolder(private val mViewMvc: SessionViewMvc<ListenerType>) :
        RecyclerView.ViewHolder(mViewMvc.rootView!!) {
        val view: SessionViewMvc<ListenerType> get() = mViewMvc
    }

    private var mSessions: List<Session> = emptyList()
    private var mExpandedSessionUUIDs: MutableSet<String> = mutableSetOf()
    private var mLoadingSessionUUIDs: MutableSet<String> = mutableSetOf()

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val session = mSessions.get(position)
        val sessionView = holder.view
        sessionView.bindSession(session)

        if (mExpandedSessionUUIDs.contains(session.uuid)) {
            sessionView.expandSessionCard()
        } else {
            sessionView.collapseSessionCard()
        }

        if (mLoadingSessionUUIDs.contains(session.uuid)) {
            sessionView.showLoader()
        } else {
            sessionView.hideLoader()
        }
    }

    override fun getItemCount(): Int {
        return mSessions.size
    }

    fun bindSessions(sessions: List<Session>) {
        mSessions = sessions
        notifyDataSetChanged()
    }

    fun expandSessionCard(session: Session) {
        mExpandedSessionUUIDs.add(session.uuid)
    }

    fun collapseSessionCard(session: Session) {
        mExpandedSessionUUIDs.remove(session.uuid)
    }

    fun showLoaderFor(session: Session) {
        mLoadingSessionUUIDs.add(session.uuid)
        notifyDataSetChanged()
    }

    fun hideLoaderFor(session: Session) {
        mLoadingSessionUUIDs.remove(session.uuid)
        notifyDataSetChanged()
    }
}
