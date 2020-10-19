package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import android.view.LayoutInflater
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session


abstract class SessionsRecyclerAdapter<ListenerType>(
    private val mInflater: LayoutInflater,
    protected val supportFragmentManager: FragmentManager
): RecyclerView.Adapter<SessionsRecyclerAdapter<ListenerType>.MyViewHolder>() {

    inner class MyViewHolder(private val mViewMvc: SessionViewMvc<ListenerType>) :
        RecyclerView.ViewHolder(mViewMvc.rootView!!) {
        val view: SessionViewMvc<ListenerType> get() = mViewMvc
    }

    private var mSessionUUIDS: List<String> = emptyList()
    private var mSessionPresenters: HashMap<String, SessionPresenter> = hashMapOf()

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val uuid = mSessionUUIDS.get(position)
        val sessionPresenter = mSessionPresenters[uuid]
        sessionPresenter?.let {
            holder.view.bindSession(sessionPresenter)
        }
    }

    override fun getItemCount(): Int {
        return mSessionPresenters.size
    }

    fun bindSessions(sessions: List<Session>) {
        mSessionUUIDS = sessions.map { session -> session.uuid }

        sessions.forEach { session ->
            if (mSessionPresenters.containsKey(session.uuid)) {
                val sessionPresenter = mSessionPresenters[session.uuid]
                sessionPresenter!!.session = session
                sessionPresenter.chartData = ChartData(session)
            } else {
                val sessionPresenter = SessionPresenter(session)
                mSessionPresenters[session.uuid] = sessionPresenter
            }
        }

        notifyDataSetChanged()
    }

    fun showLoaderFor(session: Session) {
        val sessionPresenter = mSessionPresenters[session.uuid]
        sessionPresenter?.loading = true

        notifyDataSetChanged()
    }

    fun hideLoaderFor(session: Session) {
        val sessionPresenter = mSessionPresenters[session.uuid]
        sessionPresenter?.loading = false

        notifyDataSetChanged()
    }
}
