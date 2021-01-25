package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Session

class FixedSessionConfirmationViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?,
    private val session: Session
) : ConfirmationViewMvcImpl(inflater, parent, supportFragmentManager, session) {
    override fun layoutId(): Int {
        return R.layout.fragment_fixed_session_confirmation
    }

    override fun shouldInitMap(): Boolean {
        return session.indoor == false
    }

    override fun updateLocation(latitude: Double?, longitude: Double?) {
        // do nothing, static map
    }
}
