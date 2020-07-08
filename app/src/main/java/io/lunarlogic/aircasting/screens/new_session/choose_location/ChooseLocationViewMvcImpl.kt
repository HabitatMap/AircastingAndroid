package io.lunarlogic.aircasting.screens.new_session.choose_location

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.TAGS_SEPARATOR

class ChooseLocationViewMvcImpl: BaseObservableViewMvc<ChooseLocationViewMvc.Listener>, ChooseLocationViewMvc,
    OnMapReadyCallback {
    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_choose_location, parent, false)

        val mapFragment = supportFragmentManager?.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val continueButton = rootView?.findViewById<Button>(R.id.continue_button)
        continueButton?.setOnClickListener {
            onContinueClicked()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap ?: return
        val location = LatLng(50.058191, 19.9263968) // TODO: change for current location
        with(googleMap) {
            moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13f))
            val marker = addMarker(
                MarkerOptions()
                .position(location)
                .title("Marker"))
            marker.setDraggable(true)
        }
    }

    private fun onContinueClicked() {
        for (listener in listeners) {
            listener.onContinueClicked()
        }
    }
}