package pl.llp.aircasting.ui.view.screens.new_session.session_details

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.model.Session

open class SessionDetailsControllerFactory {
    open fun get(
        mContextActivity: FragmentActivity?,
        view: SessionDetailsViewMvc?,
        sessionType: Session.Type,
        fragmentManager: FragmentManager
    ): SessionDetailsController {
        return when(sessionType) {
            Session.Type.MOBILE -> SessionDetailsController(mContextActivity, view)
            Session.Type.FIXED -> FixedSessionDetailsController(mContextActivity, view as FixedSessionDetailsViewMvcImpl, fragmentManager)
        }
    }
}
