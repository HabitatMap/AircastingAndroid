package pl.llp.aircasting.ui.view.screens.new_session.session_details

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.model.LocalSession

open class SessionDetailsControllerFactory {
    open fun get(
        mContextActivity: FragmentActivity?,
        view: SessionDetailsViewMvc?,
        localSessionType: LocalSession.Type,
        fragmentManager: FragmentManager
    ): SessionDetailsController {
        return when(localSessionType) {
            LocalSession.Type.MOBILE -> SessionDetailsController(mContextActivity, view)
            LocalSession.Type.FIXED -> FixedSessionDetailsController(mContextActivity, view as FixedSessionDetailsViewMvcImpl, fragmentManager)
        }
    }
}
