package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.findFragment
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Session

class MobileSessionConfirmationViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?,
    session: Session
) : ConfirmationViewMvcImpl(inflater, parent, supportFragmentManager, session) {

    override fun layoutId(): Int {
        return R.layout.fragment_mobile_session_confirmation
    }

    override fun updateLocation(latitude: Double?, longitude: Double?) {
        updateMarkerPosition(latitude, longitude)
    }

    override fun recordingWithoutLocation() {
        for (listener in listeners) {
            if (listener.areMapsDisabled()){
                val instructions = rootView?.findViewById<TextView>(R.id.instructions)
                instructions?.visibility = View.GONE

                val map = rootView?.findViewById<LinearLayout>(R.id.map_layout)
                map?.visibility = View.GONE
            }
        }
    }
}
