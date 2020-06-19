package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup


class MobileDormantRecyclerAdapter(
    private val mInflater: LayoutInflater,
    private val mListener: DormantSessionViewMvc.Listener
): SessionsRecyclerAdapter<DormantSessionViewMvc.Listener>(mInflater) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc = DormantSessionViewMvcImpl(mInflater, parent)
        viewMvc.registerListener(mListener)
        return MyViewHolder(viewMvc)
    }
}