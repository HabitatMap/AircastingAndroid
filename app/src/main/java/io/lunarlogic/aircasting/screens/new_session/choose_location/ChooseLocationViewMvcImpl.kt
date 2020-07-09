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


class ChooseLocationViewMvcImpl: BaseObservableViewMvc<ChooseLocationViewMvc.Listener>, ChooseLocationViewMvc,
    OnMapReadyCallback {

    private val MAX_ZOOM = 20.0f
    private val MIN_ZOOM = 5.0f

    // TODO: handle?
    private var mZoom = 13f

    // TODO: change for current location
    private val mDefaultLatitude = 50.058191
    private val mDefaultLongitude = 19.9263968

    private var mLatitude = mDefaultLatitude
    private var mLongitude = mDefaultLongitude

    private lateinit var mMarker: Marker

    private lateinit var mMap: GoogleMap

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_choose_location, parent, false)

        val autocompleteFragment =
            supportFragmentManager?.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                place.latLng?.let {
                    mLatitude = it.latitude
                    mLongitude = it.longitude
                    updateMarkerPosition()
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
        for (listener in listeners) {
            listener.onContinueClicked()
        }
    }

    private fun setZoomPreferences() {
        mMap.setMaxZoomPreference(MAX_ZOOM)
        mMap.setMinZoomPreference(MIN_ZOOM)
    }

    private fun addMarkerToMap() {
        val location = LatLng(mLatitude, mLongitude)
        mMarker = mMap.addMarker(
            MarkerOptions()
                .position(location)
                .title("Marker"))
        mMarker.setDraggable(true)
    }

    private fun updateMapCamera() {
        val newLocation = LatLng(mLatitude, mLongitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, mZoom))
    }

    private fun updateMarkerPosition() {
        mMarker.position = LatLng(mLatitude, mLongitude)
    }

    private fun resetMapToDefaults() {
        mLatitude = mDefaultLatitude
        mLongitude = mDefaultLongitude

        updateMarkerPosition()
        updateMapCamera()
    }
}