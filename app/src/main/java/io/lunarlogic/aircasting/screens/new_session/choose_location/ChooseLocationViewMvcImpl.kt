package io.lunarlogic.aircasting.screens.new_session.choose_location

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import com.google.android.gms.common.api.Status
import com.google.android.libraries.maps.model.Marker
import android.view.View
import io.lunarlogic.aircasting.sensor.Session


class ChooseLocationViewMvcImpl: BaseObservableViewMvc<ChooseLocationViewMvc.Listener>, ChooseLocationViewMvc,
    OnMapReadyCallback {

    private val session: Session

    private val MAX_ZOOM = 20.0f
    private val MIN_ZOOM = 5.0f

    // TODO: handle?
    private var mZoom = 13f

    private val mDefaultLatitude: Double
    private val mDefaultLongitude: Double

    private lateinit var mMarker: Marker

    private lateinit var mMap: GoogleMap

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?,
        session: Session
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_choose_location, parent, false)
        this.session = session

        mDefaultLatitude = session.location?.latitude ?: 40.7128
        mDefaultLongitude = session.location?.longitude ?: 74.0060
        
        val autocompleteFragment =
            supportFragmentManager?.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                place.latLng?.let {
                    updateMarkerPosition(it.latitude, it.longitude)
                    updateMapCamera()
                }
            }

            override fun onError(status: Status) {
                println("ANIA ERROR :(!") // TODO: handle
            }
        })

        autocompleteFragment.view!!.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_clear_button)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    autocompleteFragment.setText("")
                    view.setVisibility(View.GONE)
                    resetMapToDefaults()
                }
            })

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val continueButton = rootView?.findViewById<Button>(R.id.continue_button)
        continueButton?.setOnClickListener {
            onContinueClicked()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap ?: return
        mMap = googleMap
        setZoomPreferences()
        addMarkerToMap()
        updateMapCamera()
    }

    private fun onContinueClicked() {
        val latitude = mMarker.position.latitude
        val longitude = mMarker.position.longitude
        session.location = Session.Location(latitude, longitude)

        for (listener in listeners) {
            listener.onContinueClicked(session)
        }
    }

    private fun setZoomPreferences() {
        mMap.setMaxZoomPreference(MAX_ZOOM)
        mMap.setMinZoomPreference(MIN_ZOOM)
    }

    private fun addMarkerToMap() {
        val location = LatLng(mDefaultLatitude, mDefaultLongitude)
        mMarker = mMap.addMarker(
            MarkerOptions()
                .position(location))
        mMarker.setDraggable(true)
    }

    private fun updateMapCamera() {
        val newLocation = LatLng(mMarker.position.latitude, mMarker.position.longitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, mZoom))
    }

    private fun updateMarkerPosition(latitude: Double, longitude: Double) {
        mMarker.position = LatLng(latitude, longitude)
    }

    private fun resetMapToDefaults() {
        updateMarkerPosition(mDefaultLatitude, mDefaultLongitude)
        updateMapCamera()
    }
}