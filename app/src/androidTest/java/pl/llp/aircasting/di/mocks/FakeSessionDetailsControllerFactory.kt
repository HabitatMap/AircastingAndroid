package pl.llp.aircasting.di.mocks

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.new_session.session_details.FixedSessionDetailsViewMvcImpl
import pl.llp.aircasting.screens.new_session.session_details.SessionDetailsController
import pl.llp.aircasting.screens.new_session.session_details.SessionDetailsControllerFactory
import pl.llp.aircasting.screens.new_session.session_details.SessionDetailsViewMvc

class FakeSessionDetailsControllerFactory: SessionDetailsControllerFactory() {
    override fun get(mContextActivity: FragmentActivity?,
                     view: SessionDetailsViewMvc?,
                     sessionType: Session.Type,
                     fragmentManager: FragmentManager
    ): SessionDetailsController {
        if (sessionType == Session.Type.FIXED) {
            return FakeFixedSessionDetailsController(
                mContextActivity,
                view as FixedSessionDetailsViewMvcImpl
            )
        }

        return super.get(mContextActivity, view, sessionType, fragmentManager)
    }
}
