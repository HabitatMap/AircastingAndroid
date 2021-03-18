package io.lunarlogic.aircasting.di.mocks

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.di.mocks.FakeFixedSessionDetailsController
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.new_session.session_details.FixedSessionDetailsViewMvcImpl
import io.lunarlogic.aircasting.screens.new_session.session_details.SessionDetailsController
import io.lunarlogic.aircasting.screens.new_session.session_details.SessionDetailsControllerFactory
import io.lunarlogic.aircasting.screens.new_session.session_details.SessionDetailsViewMvc

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
