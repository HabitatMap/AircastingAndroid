package io.lunarlogic.aircasting.screens.dashboard.following

import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvc
import io.lunarlogic.aircasting.sensor.Session

interface FollowingSessionViewMvc:
    SessionViewMvc<FollowingSessionViewMvc.Listener> {
    interface Listener {
        fun onMapButtonClicked(session: Session)
    }
}
