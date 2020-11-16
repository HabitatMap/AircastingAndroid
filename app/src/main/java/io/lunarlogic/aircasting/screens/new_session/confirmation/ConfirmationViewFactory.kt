package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.models.Session

class ConfirmationViewFactory() {
    companion object {
        fun get(
            inflater: LayoutInflater,
            container: ViewGroup?,
            supportFragmentManager: FragmentManager?,
            session: Session
        ): ConfirmationViewMvc {
            return when(session.type) {
                Session.Type.MOBILE -> MobileSessionConfirmationViewMvcImpl(
                    inflater, container, supportFragmentManager, session)
                Session.Type.FIXED -> FixedSessionConfirmationViewMvcImpl(
                    inflater, container, supportFragmentManager, session)
            }
        }
    }
}
