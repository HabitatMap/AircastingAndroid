package pl.llp.aircasting.ui.view.screens.new_session.confirmation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session

class FixedSessionConfirmationViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?,
    session: Session,
    areMapsDisabled: Boolean
) : ConfirmationViewMvcImpl(inflater, parent, supportFragmentManager, session, areMapsDisabled) {
    override fun layoutId(): Int {
        return R.layout.fragment_fixed_session_confirmation
    }

    override fun shouldInitMap(): Boolean {
        return session?.indoor == false
    }

    override fun updateLocation(latitude: Double?, longitude: Double?) {
        // do nothing, static map
    }

}
