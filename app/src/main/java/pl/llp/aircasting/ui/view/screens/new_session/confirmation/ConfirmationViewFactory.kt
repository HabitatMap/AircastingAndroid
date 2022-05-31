package pl.llp.aircasting.ui.view.screens.new_session.confirmation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.model.LocalSession

class ConfirmationViewFactory {
    companion object {
        fun get(
            inflater: LayoutInflater,
            container: ViewGroup?,
            supportFragmentManager: FragmentManager?,
            localSession: LocalSession,
            areMapsDisabled: Boolean
        ): ConfirmationViewMvcImpl {
            return when(localSession.type) {
                LocalSession.Type.MOBILE -> MobileSessionConfirmationViewMvcImpl(
                    inflater, container, supportFragmentManager, localSession, areMapsDisabled)
                LocalSession.Type.FIXED -> FixedSessionConfirmationViewMvcImpl(
                    inflater, container, supportFragmentManager, localSession, areMapsDisabled)
            }
        }
    }
}
