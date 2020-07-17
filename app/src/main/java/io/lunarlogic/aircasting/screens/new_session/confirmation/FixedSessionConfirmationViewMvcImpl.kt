package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.sensor.Session

class FixedSessionConfirmationViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?,
    session: Session
) : ConfirmationViewMvcImpl(inflater, parent, supportFragmentManager, session) {

    override fun layoutId(): Int {
        return R.layout.fragment_fixed_session_confirmation
    }

    override fun descriptionStringId(): Int {
        return R.string.fixed_session_confirmation_description
    }

    override fun updateLocation(latitude: Double?, longitude: Double?) {
        // do nothing, static map
    }
}