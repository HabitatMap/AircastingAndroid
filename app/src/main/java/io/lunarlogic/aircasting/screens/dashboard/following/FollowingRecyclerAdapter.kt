package io.lunarlogic.aircasting.screens.dashboard.following

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter


class FollowingRecyclerAdapter(
    private val mInflater: LayoutInflater,
    private val mListener: FollowingSessionViewMvc.Listener,
    context: Context,
    supportFragmentManager: FragmentManager
): SessionsRecyclerAdapter<FollowingSessionViewMvc.Listener>(mInflater, context, supportFragmentManager) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc =
            FollowingSessionViewMvcImpl(
                mInflater,
                parent,
                context,
                supportFragmentManager
            )
        viewMvc.registerListener(mListener)
        return MyViewHolder(viewMvc)
    }
}
