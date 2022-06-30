package pl.llp.aircasting.ui.view.screens.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.app_bar.view.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.response.search.SessionInRegionResponse
import pl.llp.aircasting.data.api.response.search.SessionsInRegionsRes
import pl.llp.aircasting.data.api.util.Ozone
import pl.llp.aircasting.data.api.util.ParticulateMatter
import pl.llp.aircasting.data.api.util.SensorInformation
import pl.llp.aircasting.data.api.util.StringConstants
import pl.llp.aircasting.data.model.GeoSquare
import pl.llp.aircasting.databinding.ActivitySearchFollowResultBinding
import pl.llp.aircasting.ui.view.adapters.FixedFollowAdapter
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import pl.llp.aircasting.util.*
import javax.inject.Inject

class SearchFixedResultActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraMoveStartedListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var searchFollowViewModel: SearchFollowViewModel
    private lateinit var adapter: FixedFollowAdapter

    private lateinit var binding: ActivitySearchFollowResultBinding
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private lateinit var mMap: GoogleMap
    private var placesClient: PlacesClient? = null
    private val bottomSheetDialog: SearchFixedBottomSheet by lazy { SearchFixedBottomSheet() }

    private lateinit var address: String
    private lateinit var mLat: String
    private lateinit var mLng: String
    private var mSelectedMarker: Marker? = null

    private val options = MarkerOptions()
    private var txtParameter: String? = null
    private var txtSensor: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_follow_result)

        (application as AircastingApplication)
            .appComponent.inject(this)
        searchFollowViewModel =
            ViewModelProvider(this, viewModelFactory)[SearchFollowViewModel::class.java]

        setupUI()
    }

    private fun setupUI() {
        setSupportActionBar(topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupMapView()

        getIntentsFromThePreviousActivity()

        binding.include.finishView.visible()

        binding.txtShowing.text = getString(R.string.showing_results_for) + " " + txtParameter
        binding.txtUsing.text = getString(R.string.using_txt) + " " + getSensor()

        binding.btnRedo.setOnClickListener { resetTheSearch() }
        binding.include.finishView.setOnClickListener { goToDashboard() }

        setupRecyclerView()
        setupSearchLayout()
    }

    private fun setupMapView() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun getIntentsFromThePreviousActivity() {
        address = intent.getStringExtra("address").toString()

        mLat = intent.getStringExtra("lat").toString()
        mLng = intent.getStringExtra("lng").toString()

        txtParameter = intent.getStringExtra("txtParameter")
        txtSensor = intent.getStringExtra("txtSensor")
    }

    private fun setupSearchLayout() {
        autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.place_autocomplete_results) as AutocompleteSupportFragment

        autocompleteFragment.apply {
            val etPlace =
                view?.findViewById<EditText>(R.id.places_autocomplete_search_input)
            findViewById<ImageButton>(R.id.places_autocomplete_search_button)?.gone()

            etPlace?.setStyle(address, R.color.black_color)

            initialisePlacesClient()

            setPlaceFields(listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG))

            setupOnPlaceSelectedListener(etPlace)
        }
    }

    private fun initialisePlacesClient() {
        initializePlacesApi(this)
        placesClient = Places.createClient(this)
    }

    private fun AutocompleteSupportFragment.setupOnPlaceSelectedListener(etPlace: EditText?) {
        setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                address = place.address as String
                val lat = place.latLng?.latitude
                val lng = place.latLng?.longitude

                etPlace?.setStyle(address, R.color.black_color)
                if (lat != null && lng != null) {
                    moveMapToSelectedLocationAndRefresh(lat, lng)
                }
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Log.d("onError", status.statusMessage.toString())
            }
        })
    }

    private fun goToDashboard() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun setupRecyclerView() {
        adapter = FixedFollowAdapter(this::showBottomSheetDialog)
        binding.recyclerFixedFollow.adapter = adapter
    }

    private fun setupObserverForApiCallWithCoordinatesAndSensor(
        square: GeoSquare,
        sensorInfo: SensorInformation
    ) {
        searchFollowViewModel.getSessionsInRegion(square, sensorInfo).observe(this) {
            when (it.status) {
                Status.SUCCESS -> updateUI(it)
                Status.ERROR -> {
                    binding.progressBar.inVisible()
                    showToast(it.message.toString())
                }
                Status.LOADING -> binding.progressBar.visible()
            }
        }
    }

    private fun updateUI(it: Resource<SessionsInRegionsRes>) {
        binding.apply {
            progressBar.inVisible()
            txtShowingSessionsNumber.visible()
        }
        it.data?.let { data ->
            val sessions = data.sessions
            val count = data.fetchableSessionsCount

            setupMapMarkers(sessions)

            updateText(count)

            refreshAdapterDataSet(sessions)
        }
    }

    private fun updateText(count: Int) {
        if (count != 0) binding.txtShowingSessionsNumber.text =
            getString(R.string.txt_showing_sessions_number, count, count)
        else
            binding.txtShowingSessionsNumber.apply {
                text = getString(R.string.txt_showing_sessions_number, count, count)
                setMargins(bottom = 50)
            }
    }

    private fun setupMapMarkers(sessions: List<SessionInRegionResponse>) {
        for (i in sessions.indices) {
            val getLats = sessions[i].latitude
            val getLngs = sessions[i].longitude
            val uuid = sessions[i].uuid
            mMap.drawMarkerOnMap(this, options, getLats, getLngs, uuid)
        }
    }

    private fun refreshAdapterDataSet(mySessions: List<SessionInRegionResponse>) {
        adapter.refresh(mySessions)
        adapter.notifyDataSetChanged()
    }

    private fun resetTheSearch() {
        searchSessionsInMapArea()

        binding.btnRedo.gone()
    }

    private fun showBottomSheetDialog(session: SessionInRegionResponse) {
        passLatLng(session)

        searchFollowViewModel.selectSession(session)
        bottomSheetDialog.show(supportFragmentManager)
    }

    private fun passLatLng(session: SessionInRegionResponse) {
        searchFollowViewModel.getLat(session.latitude)
        searchFollowViewModel.getLng(session.longitude)
    }

    private fun getSensorInfo(): SensorInformation {
        return when (txtSensor) {
            StringConstants.airbeam2sensorName -> ParticulateMatter.AIRBEAM2
            StringConstants.openAQsensorNamePM -> ParticulateMatter.OPEN_AQ
            StringConstants.purpleAirSensorName -> ParticulateMatter.PURPLE_AIR
            StringConstants.openAQsensorNameOzone -> Ozone.OPEN_AQ
            else -> ParticulateMatter.AIRBEAM2
        }
    }

    private fun getSensor(): String {
        return when (txtSensor) {
            StringConstants.airbeam2sensorName -> StringConstants.airbeam
            StringConstants.openAQsensorNamePM -> StringConstants.openAQ
            StringConstants.purpleAirSensorName -> StringConstants.purpleAir
            StringConstants.openAQsensorNameOzone -> StringConstants.openAQ
            else -> StringConstants.airbeam
        }
    }

    private fun moveMapToSelectedLocationAndRefresh(lat: Double, long: Double) {
        mMap.clear()

        val selectedLocation = LatLng(lat, long)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 10f))

        binding.btnRedo.gone()
        searchSessionsInMapArea()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        styleGoogleMap(mMap, this)

        val lat = mLat.toDouble()
        val lng = mLng.toDouble()

        val theLocation = LatLng(lat, lng)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(theLocation, 10f))

        searchSessionsInMapArea()

        mMap.setOnMarkerClickListener(this)
        mMap.setOnCameraMoveStartedListener(this)
    }

    private fun searchSessionsInMapArea() {
        val north = mMap.projection.visibleRegion.farLeft.latitude
        val west = mMap.projection.visibleRegion.farLeft.longitude
        val south = mMap.projection.visibleRegion.nearRight.latitude
        val east = mMap.projection.visibleRegion.nearRight.longitude

        val square = GeoSquare(north, south, east, west)
        val sensorInfo = getSensorInfo()

        setupObserverForApiCallWithCoordinatesAndSensor(square, sensorInfo)
    }

    private fun setMarkerIconToDefault(marker: Marker) {
        marker.setIcon(getBitmapDescriptorFromVector(this, R.drawable.map_dot_with_circle_inside))
    }

    private fun highlightMarkerIcon(marker: Marker) {
        marker.setIcon(getBitmapDescriptorFromVector(this, R.drawable.map_dot_selected))
    }

    private fun selectCorrespondingCardView(marker: Marker) {
        val uuid = marker.snippet.toString()
        val position = adapter.getSessionPositionBasedOnId(uuid)

        binding.recyclerFixedFollow.scrollToPosition(position)
        adapter.addCardBorder(position)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (mSelectedMarker != null) {
            setMarkerIconToDefault(mSelectedMarker!!)
            mSelectedMarker = null
        }
        mSelectedMarker = marker
        highlightMarkerIcon(marker)

        selectCorrespondingCardView(marker)
        return false
    }

    override fun onCameraMoveStarted(p0: Int) {
        binding.btnRedo.visible()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}