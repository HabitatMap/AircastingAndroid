package pl.llp.aircasting.ui.view.screens.dashboard.following

import pl.llp.aircasting.ui.view.screens.dashboard.fixed.FixedSessionViewMvc

interface FollowingSessionViewMvc: FixedSessionViewMvc {
    interface Listener : FixedSessionViewMvc.Listener
}
