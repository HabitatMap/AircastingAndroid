package io.lunarlogic.aircasting.screens.dashboard.mobile

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.screens.dashboard.ActiveSessionViewMvcImpl

class MobileActiveSessionViewMvcImpl(inflater: LayoutInflater, parent: ViewGroup):
    ActiveSessionViewMvcImpl<MobileActiveSessionViewMvc.Listener>(inflater, parent),
    MobileActiveSessionViewMvc {
}
