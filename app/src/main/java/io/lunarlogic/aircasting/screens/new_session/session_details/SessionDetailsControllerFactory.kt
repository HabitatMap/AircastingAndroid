package io.lunarlogic.aircasting.screens.new_session.session_details

import android.content.Context
import io.lunarlogic.aircasting.sensor.Session

open class SessionDetailsControllerFactory {
    open fun get(
        context: Context?,
        view: SessionDetailsViewMvc,
        sessionType: Session.Type
    ): SessionDetailsController {
        return when(sessionType) {
            Session.Type.MOBILE -> SessionDetailsController(context, view)
            Session.Type.FIXED -> FixedSessionDetailsController(context, view as FixedSessionDetailsViewMvcImpl)
        }
    }
}
