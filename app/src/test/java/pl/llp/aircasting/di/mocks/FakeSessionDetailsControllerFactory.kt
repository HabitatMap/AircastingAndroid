package pl.llp.aircasting.di.mocks

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.screens.new_session.session_details.FixedSessionDetailsViewMvcImpl
import pl.llp.aircasting.ui.view.screens.new_session.session_details.SessionDetailsController
import pl.llp.aircasting.ui.view.screens.new_session.session_details.SessionDetailsControllerFactory
import pl.llp.aircasting.ui.view.screens.new_session.session_details.SessionDetailsViewMvc

class FakeSessionDetailsControllerFactory: SessionDetailsControllerFactory() {
    override fun get(mContextActivity: FragmentActivity?,
                     view: SessionDetailsViewMvc?,
                     localSessionType: LocalSession.Type,
                     fragmentManager: FragmentManager
    ): SessionDetailsController {
        if (localSessionType == LocalSession.Type.FIXED) {
            return FakeFixedSessionDetailsController(
                mContextActivity,
                view as FixedSessionDetailsViewMvcImpl
            )
        }

        return super.get(mContextActivity, view, localSessionType, fragmentManager)
    }
}
