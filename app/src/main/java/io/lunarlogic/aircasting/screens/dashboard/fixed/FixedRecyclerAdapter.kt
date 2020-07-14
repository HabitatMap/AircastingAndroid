package io.lunarlogic.aircasting.screens.dashboard.fixed

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter


class FixedRecyclerAdapter(
    private val mInflater: LayoutInflater,
    private val mListener: FixedSessionViewMvc.Listener
): SessionsRecyclerAdapter<FixedSessionViewMvc.Listener>(mInflater) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc =
            FixedSessionViewMvcImpl(
                mInflater,
                parent
            )
        viewMvc.registerListener(mListener)
        return MyViewHolder(viewMvc)
    }
}