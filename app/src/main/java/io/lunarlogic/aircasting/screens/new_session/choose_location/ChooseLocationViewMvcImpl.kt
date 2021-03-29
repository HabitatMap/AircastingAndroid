package io.lunarlogic.aircasting.screens.new_session.choose_location

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import com.google.android.gms.common.api.Status
import com.google.android.libraries.maps.*
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.exceptions.ChooseAirBeamLocationSelectingPlaceError
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc


class ChooseLocationViewMvcImpl: BaseObservableViewMvc<ChooseLocationViewMvc.Listener>, ChooseLocationViewMvc,
    OnMapReadyCallback {

    private val session: Session

    private val MAX_ZOOM = 20.0f
    private val MIN_ZOOM = 5.0f
    private val DEFAULT_ZOOM = 13f

    private val mDefaultLatitude: Double
    private val mDefaultLongitude: Double

    private var mMap: GoogleMap? = null
    private var mMapFragment: SupportMapFragment? = null
    private var mSupportFragmentManager: FragmentManager?

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?,
        session: Session,
        errorHandler: ErrorHandler
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_choose_location, parent, false)
        this.session = session
        this.mSupportFragmentManager = supportFragmentManager

        mDefaultLatitude = session.location?.latitude ?: Session.Location.DEFAULT_LOCATION.latitude
        mDefaultLongitude = session.location?.longitude ?: Session.Location.DEFAULT_LOCATION.longitude

        val autocompleteFragment =
            supportFragmentManager?.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                place.latLng?.let {
                    updateMapCamera(it.latitude, it.longitude)
                }
            }

            override fun onError(status: Status) {
                errorHandler.handle(ChooseAirBeamLocationSelectingPlaceError())
            }
        })

        autocompleteFragment.requireView().findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_clear_button)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    autocompleteFragment.setText("")
                    view.setVisibility(View.GONE)
                    resetMapToDefaults()
                }
            })

        mMapFragment = SupportMapFragment.newInstance(mapOptions())
        mMapFragment?.let {
            supportFragmentManager?.beginTransaction()?.replace(R.id.map, it)?.commit()
        }
        mMapFragment?.getMapAsync(this)

        val continueButton = rootView?.findViewById<Button>(R.id.continue_button)
        continueButton?.setOnClickListener {
            onContinueClicked()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap ?: return
        mMap = googleMap
        setZoomPreferences()
        resetMapToDefaults()
    }

    override fun onDestroy() {
        mMap = null
        mMapFragment?.onDestroy()
        mMapFragment?.let {
            mSupportFragmentManager?.beginTransaction()?.remove(it)?.commitAllowingStateLoss()
        }
        mMapFragment = null
    }

    private fun onContinueClicked() {
        var location: Session.Location? = Session.Location.FAKE_LOCATION

        mMap?.let { map ->
            val target = map.cameraPosition.target
            val latitude = target.latitude
            val longitude = target.longitude
            location = Session.Location(latitude, longitude)
        }
        session.location = location

        for (listener in listeners) {
            listener.onContinueClicked(session)
        }
    }

    private fun setZoomPreferences() {
        mMap?.setMaxZoomPreference(MAX_ZOOM)
        mMap?.setMinZoomPreference(MIN_ZOOM)
    }

    private fun updateMapCamera(latitude: Double, longitude: Double, aZoom: Float? = null) {
        val zoom = aZoom ?: mMap?.cameraPosition?.zoom
        val newLocation = LatLng(latitude, longitude)
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, zoom ?: 0f))
    }

    private fun resetMapToDefaults() {
        updateMapCamera(mDefaultLatitude, mDefaultLongitude, DEFAULT_ZOOM)
    }

    private fun mapOptions(): GoogleMapOptions {
        val mapOptions = GoogleMapOptions()
        mapOptions.useViewLifecycleInFragment(true)
        mapOptions.zoomControlsEnabled(true)
        mapOptions.zoomGesturesEnabled(true)

        return mapOptions
    }
}
