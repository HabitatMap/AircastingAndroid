package io.lunarlogic.aircasting.screens.dashboard.mobile

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter


class MobileActiveRecyclerAdapter(
    private val mInflater: LayoutInflater,
    private val mListener: MobileActiveSessionViewMvc.Listener
): SessionsRecyclerAdapter<MobileActiveSessionViewMvc.Listener>(mInflater) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc =
            MobileMobileActiveSessionViewMvcImpl(
                mInflater,
                parent
            )
        viewMvc.registerListener(mListener)
        return MyViewHolder(viewMvc)
    }
}