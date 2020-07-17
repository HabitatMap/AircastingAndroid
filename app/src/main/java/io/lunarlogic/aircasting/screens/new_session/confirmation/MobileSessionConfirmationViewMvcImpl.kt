package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.libraries.maps.model.LatLng
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.sensor.Session

class MobileSessionConfirmationViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?,
    session: Session
) : ConfirmationViewMvcImpl(inflater, parent, supportFragmentManager, session) {

    override fun layoutId(): Int {
        return R.layout.fragment_mobile_session_confirmation
    }

    override fun descriptionStringId(): Int {
        return R.string.mobile_session_confirmation_description
    }

    override fun updateLocation(latitude: Double?, longitude: Double?) {
        updateMarkerPosition(latitude, longitude)
    }
}