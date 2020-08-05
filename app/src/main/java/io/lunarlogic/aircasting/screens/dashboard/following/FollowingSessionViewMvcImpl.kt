package io.lunarlogic.aircasting.screens.dashboard.following

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.screens.dashboard.ActiveSessionViewMvcImpl

class FollowingSessionViewMvcImpl(inflater: LayoutInflater, parent: ViewGroup):
    ActiveSessionViewMvcImpl<FollowingSessionViewMvc.Listener>(inflater, parent),
    FollowingSessionViewMvc {
}
