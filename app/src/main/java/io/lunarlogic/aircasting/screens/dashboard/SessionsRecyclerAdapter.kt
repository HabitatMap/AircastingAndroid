package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import io.lunarlogic.aircasting.sensor.Session


abstract class SessionsRecyclerAdapter<ListenerType>(
    private val mInflater: LayoutInflater
): RecyclerView.Adapter<SessionsRecyclerAdapter<ListenerType>.MyViewHolder>() {

    inner class MyViewHolder(private val mViewMvc: SessionViewMvc<ListenerType>) :
        RecyclerView.ViewHolder(mViewMvc.rootView!!) {
        val view: SessionViewMvc<ListenerType> get() = mViewMvc
    }

    private var mSessions: List<Session> = emptyList()

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.view.bindSession(mSessions.get(position))
    }

    override fun getItemCount(): Int {
        return mSessions.size
    }

    fun bindSessions(sessions: List<Session>) {
        mSessions = sessions
        notifyDataSetChanged()
    }
}