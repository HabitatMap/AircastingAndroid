package io.lunarlogic.aircasting.screens.dashboard.following

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter


class FollowingRecyclerAdapter(
    private val mInflater: LayoutInflater,
    private val mListener: FollowingSessionViewMvc.Listener
): SessionsRecyclerAdapter<FollowingSessionViewMvc.Listener>(mInflater) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc =
            FollowingSessionViewMvcImpl(
                mInflater,
                parent
            )
        viewMvc.registerListener(mListener)
        return MyViewHolder(viewMvc)
    }
}