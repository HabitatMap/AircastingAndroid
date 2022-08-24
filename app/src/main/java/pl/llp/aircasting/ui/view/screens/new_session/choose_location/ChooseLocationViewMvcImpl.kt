package pl.llp.aircasting.ui.view.screens.new_session.choose_location

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.FragmentManager
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ChooseAirBeamLocationSelectingPlaceError
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.setMapType

class ChooseLocationViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    private val supportFragmentManager: FragmentManager?,
    private val session: Session,
    private val errorHandler: ErrorHandler
) : BaseObservableViewMvc<ChooseLocationViewMvc.Listener>(),
    ChooseLocationViewMvc,
    OnMapReadyCallback {

    private val MAX_ZOOM = 20.0f
    private val MIN_ZOOM = 5.0f
    private val DEFAULT_ZOOM = 13f

    private val mDefaultLatitude: Double
    private val mDefaultLongitude: Double

    private var mMap: GoogleMap? = null
    private var mMapFragment: SupportMapFragment? = null
    private var mSupportFragmentManager: FragmentManager? = supportFragmentManager
    private var mSettings: Settings
    private var mApplication: AircastingApplication
    private var mContext: Context

    init {
        this.rootView = inflater.inflate(R.layout.fragment_choose_location, parent, false)
        mApplication = context.applicationContext as AircastingApplication

        mSettings = mApplication.settings
        mContext = context

        mDefaultLatitude = session.location?.latitude ?: Session.Location.DEFAULT_LOCATION.latitude
        mDefaultLongitude =
            session.location?.longitude ?: Session.Location.DEFAULT_LOCATION.longitude

        setupAutoCompleteFragment()

        setupMapFragment()

        rootView?.findViewById<Button>(R.id.continue_button)?.setOnClickListener {
            onContinueClicked()
        }
    }

    private fun setupMapFragment() {
        mMapFragment = SupportMapFragment.newInstance(mapOptions())
        mMapFragment?.let {
            supportFragmentManager?.beginTransaction()?.replace(R.id.map, it)?.commit()
        }
        mMapFragment?.getMapAsync(this)
    }

    private fun setupAutoCompleteFragment() {
        (supportFragmentManager?.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment)
            .apply {
                setPlaceFields(
                    listOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.LAT_LNG
                    )
                )
                setOnPlaceSelectedListener(object : PlaceSelectionListener {
                    override fun onPlaceSelected(place: Place) {
                        place.latLng?.let {
                            updateMapCamera(it.latitude, it.longitude)
                        }
                    }

                    override fun onError(status: Status) {
                        errorHandler.handle(ChooseAirBeamLocationSelectingPlaceError())
                    }
                })
                findViewById<View>(R.id.places_autocomplete_clear_button)
                    .setOnClickListener { view ->
                        this.setText("")
                        view.visibility = View.GONE
                        resetMapToDefaults()
                    }
                view?.findViewById(R.id.places_autocomplete_search_input) as EditText
            }
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
        mMap?.let { map ->
            val target = map.cameraPosition.target
            val latitude = target.latitude
            val longitude = target.longitude
            session.location = Session.Location(latitude, longitude)
        }

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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.setMapType(mSettings, mContext)

        setZoomPreferences()
        resetMapToDefaults()
    }
}
