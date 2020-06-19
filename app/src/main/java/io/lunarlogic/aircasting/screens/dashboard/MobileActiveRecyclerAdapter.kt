package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup


class MobileActiveRecyclerAdapter(
    private val mInflater: LayoutInflater,
    private val mListener: ActiveSessionViewMvc.Listener
): SessionsRecyclerAdapter<ActiveSessionViewMvc.Listener>(mInflater) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc = ActiveSessionViewMvcImpl(mInflater, parent)
        viewMvc.registerListener(mListener)
        return MyViewHolder(viewMvc)
    }
}