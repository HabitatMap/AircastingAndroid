package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.lunarlogic.aircasting.sensor.Session


class SessionsRecyclerAdapter(private val mInflater: LayoutInflater, private val mListener: SessionViewMvc.Listener):
    RecyclerView.Adapter<SessionsRecyclerAdapter.MyViewHolder>(), SessionViewMvc.Listener {
    class MyViewHolder(private val mViewMvc: SessionViewMvc) :
        RecyclerView.ViewHolder(mViewMvc.rootView!!) {
        val view: SessionViewMvc get() = mViewMvc
    }

    private var mSessions: List<Session> = emptyList()

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.view.bindSession(mSessions.get(position))
    }

    override fun getItemCount(): Int {
        return mSessions.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc = SessionViewMvcImpl(mInflater, parent)
        viewMvc.registerListener(this)
        return MyViewHolder(viewMvc)
    }

    fun bindSessions(sessions: List<Session>) {
        mSessions = sessions
        notifyDataSetChanged()
    }

    override fun onSessionStopClicked(session: Session) {
        mListener.onSessionStopClicked(session)
    }

    override fun onSessionDeleteClicked(session: Session) {
        mListener.onSessionDeleteClicked(session)
    }
}