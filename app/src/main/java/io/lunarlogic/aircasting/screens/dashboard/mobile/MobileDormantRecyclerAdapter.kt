package io.lunarlogic.aircasting.screens.dashboard.mobile

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter


class MobileDormantRecyclerAdapter(
    private val mInflater: LayoutInflater,
    private val mListener: MobileDormantSessionViewMvc.Listener
): SessionsRecyclerAdapter<MobileDormantSessionViewMvc.Listener>(mInflater) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc =
            MobileDormantSessionViewMvcImpl(
                mInflater,
                parent
            )
        viewMvc.registerListener(mListener)
        return MyViewHolder(viewMvc)
    }
}