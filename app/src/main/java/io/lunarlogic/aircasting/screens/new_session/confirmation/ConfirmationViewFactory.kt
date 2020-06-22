package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.screens.dashboard.*
import io.lunarlogic.aircasting.sensor.Session

class ConfirmationViewFactory() {
    companion object {
        fun get(
            inflater: LayoutInflater,
            container: ViewGroup?,
            session: Session
        ): ConfirmationViewMvc {
            return when(session.type) {
                Session.Type.MOBILE -> MobileSessionConfirmationViewMvcImpl(inflater, container, session)
                Session.Type.FIXED -> FixedSessionConfirmationViewMvcImpl(inflater, container, session)
            }
        }
    }
}