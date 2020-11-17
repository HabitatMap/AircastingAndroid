package io.lunarlogic.aircasting.screens.dashboard.mobile

import io.lunarlogic.aircasting.screens.dashboard.SessionCardListener
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvc
import io.lunarlogic.aircasting.models.Session

interface MobileActiveSessionViewMvc:
    SessionViewMvc<MobileActiveSessionViewMvc.Listener> {
    interface Listener: SessionCardListener {
        fun onSessionStopClicked(session: Session)
    }
}
