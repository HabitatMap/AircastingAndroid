package pl.llp.aircasting.ui.view.screens.new_session.confirmation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.LocalSession

class MobileSessionConfirmationViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?,
    localSession: LocalSession,
    areMapsDisabled: Boolean
) : ConfirmationViewMvcImpl(inflater, parent, supportFragmentManager, localSession, areMapsDisabled) {
    override fun layoutId(): Int {
        return R.layout.fragment_mobile_session_confirmation
    }

    override fun shouldInitMap(): Boolean {
        return !areMapsDisabled
    }

    override fun updateLocation(latitude: Double?, longitude: Double?) {
        updateMarkerPosition(latitude, longitude)
    }
}
