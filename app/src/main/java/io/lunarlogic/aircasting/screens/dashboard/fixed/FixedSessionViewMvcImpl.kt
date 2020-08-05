package io.lunarlogic.aircasting.screens.dashboard.fixed

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvcImpl

class FixedSessionViewMvcImpl(inflater: LayoutInflater, parent: ViewGroup):
    SessionViewMvcImpl<FixedSessionViewMvc.Listener>(inflater, parent),
    FixedSessionViewMvc {

    override fun layoutId(): Int {
        return R.layout.dormant_session
    }
}
