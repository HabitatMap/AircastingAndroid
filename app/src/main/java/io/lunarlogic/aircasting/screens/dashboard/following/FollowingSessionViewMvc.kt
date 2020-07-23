package io.lunarlogic.aircasting.screens.dashboard.following

import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvc

interface FollowingSessionViewMvc:
    SessionViewMvc<FollowingSessionViewMvc.Listener> {
    interface Listener {}
}