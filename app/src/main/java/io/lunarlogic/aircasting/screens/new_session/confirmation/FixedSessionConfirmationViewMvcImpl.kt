package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Session

class FixedSessionConfirmationViewMvcImpl : ConfirmationViewMvcImpl {
    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?,
        session: Session
    ): super(inflater, parent, supportFragmentManager, session) {
        if (session.indoor == true) {
            val instructions = rootView?.findViewById<TextView>(R.id.instructions)
            instructions?.visibility = View.GONE

            val map = rootView?.findViewById<View>(R.id.map)
            map?.visibility = View.GONE
        }
    }

    override fun layoutId(): Int {
        return R.layout.fragment_fixed_session_confirmation
    }

    override fun updateLocation(latitude: Double?, longitude: Double?) {
        // do nothing, static map
    }
}
