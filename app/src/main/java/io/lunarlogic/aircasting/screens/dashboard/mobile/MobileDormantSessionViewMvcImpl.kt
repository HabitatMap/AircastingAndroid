package io.lunarlogic.aircasting.screens.dashboard.mobile

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvcImpl

class MobileDormantSessionViewMvcImpl(inflater: LayoutInflater, parent: ViewGroup):
    SessionViewMvcImpl<MobileDormantSessionViewMvc.Listener>(inflater, parent),
    MobileDormantSessionViewMvc {

    override fun layoutId(): Int {
        return R.layout.dormant_session
    }
}
