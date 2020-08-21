package io.lunarlogic.aircasting.screens.new_session.session_details

import android.content.Context
import io.lunarlogic.aircasting.sensor.Session

class SessionDetailsControllerFactory {
    companion object {
        fun get(
            context: Context?,
            view: SessionDetailsViewMvc,
            sessionType: Session.Type
        ): SessionDetailsController {
            return when(sessionType) {
                Session.Type.MOBILE -> SessionDetailsController(context, view)
                Session.Type.FIXED -> FixedSessionDetailsController(context, view)
            }
        }
    }
}
