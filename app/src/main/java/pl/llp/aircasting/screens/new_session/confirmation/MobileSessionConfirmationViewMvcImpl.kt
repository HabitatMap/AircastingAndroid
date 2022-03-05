package pl.llp.aircasting.screens.new_session.confirmation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.BitmapHelper
import pl.llp.aircasting.models.Session

class MobileSessionConfirmationViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?,
    session: Session,
    areMapsDisabled: Boolean
) : ConfirmationViewMvcImpl(inflater, parent, supportFragmentManager, session, areMapsDisabled) {
    override fun layoutId(): Int {
        return R.layout.fragment_mobile_session_confirmation
    }

    override fun shouldInitMap(): Boolean {
        return !areMapsDisabled
    }

    override fun updateLocation(latitude: Double?, longitude: Double?) {
        updateMarkerPosition(latitude, longitude)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val sessionLocation = session?.location ?: return
        val location = LatLng(sessionLocation.latitude, sessionLocation.longitude)
        val icon = BitmapHelper.bitmapFromVector(context, R.drawable.ic_dot_20)
        val marker = MarkerOptions()
            .position(location)
            .icon(icon)
        mMarker = googleMap.addMarker(marker)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM))
    }
}
