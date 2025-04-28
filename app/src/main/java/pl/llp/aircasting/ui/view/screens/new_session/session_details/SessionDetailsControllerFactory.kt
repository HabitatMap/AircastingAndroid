package pl.llp.aircasting.ui.view.screens.new_session.session_details

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.model.Session

open class SessionDetailsControllerFactory {
    open fun get(
        fragment: Fragment?,
        view: SessionDetailsViewMvc?,
        sessionType: Session.Type,
        fragmentManager: FragmentManager
    ): SessionDetailsController {
        return when(sessionType) {
            Session.Type.MOBILE -> SessionDetailsController(view)
            Session.Type.FIXED -> FixedSessionDetailsController(fragment, view as FixedSessionDetailsViewMvcImpl, fragmentManager)
        }
    }
}
