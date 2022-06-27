package pl.llp.aircasting.ui.view.screens.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import pl.llp.aircasting.data.api.util.SensorNames
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
    private var placesClient: PlacesClient? = null
    private lateinit var mMap: GoogleMap

    private val bottomSheetDialog: SearchFixedBottomSheet by lazy { SearchFixedBottomSheet() }

    private val options = MarkerOptions()
    private var txtParameter: String? = null
    private var txtSensor: String? = null
    private var address: String? = null
    private var lat: String? = null
    private var lng: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_follow_result)

        (application as AircastingApplication)
            .appComponent.inject(this)
        searchFollowViewModel =
            ViewModelProvider(this, viewModelFactory)[SearchFollowViewModel::class.java]

        setupUI()
        setupAutoComplete()
        passLatLng()
    }

    private fun setupUI() {
        setSupportActionBar(topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.include.finishView.visible()

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        lat = intent.getStringExtra("lat")
        lng = intent.getStringExtra("long")
        txtParameter = intent.getStringExtra("txtParameter")
        txtSensor = intent.getStringExtra("txtSensor")
        address = intent.getStringExtra("address")

        binding.txtShowing.text = getString(R.string.showing_results_for) + " " + txtParameter
        binding.txtUsing.text = getString(R.string.using_txt) + " " + getSensor()

        binding.btnRedo.setOnClickListener { resetTheSearch() }
        binding.include.finishView.setOnClickListener { goToDashboard() }

        setupRecyclerView()
        setupSearchLayout()
    }

    private fun setupSearchLayout() {
        autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.place_autocomplete_results) as AutocompleteSupportFragment

        autocompleteFragment.apply {
            view?.apply {
                findViewById<EditText>(R.id.places_autocomplete_search_input)?.apply {
                    hint = address
                    textSize = 15.0f
                    setHintTextColor(ContextCompat.getColor(context, R.color.black_color))
                }
                findViewById<ImageButton>(R.id.places_autocomplete_search_button)?.gone()
            }
        }
    }

    private fun setupAutoComplete() {
        initialisePlacesClient()

        autocompleteFragment.apply {
            setPlaceFields(
                listOf(
                    Place.Field.ID,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
                )
            )

            val etPlace = view?.findViewById(R.id.places_autocomplete_search_input) as EditText
            setupOnPlaceSelectedListener(etPlace)
        }
    }

    private fun AutocompleteSupportFragment.setupOnPlaceSelectedListener(etPlace: EditText) {
        setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                address = place.address?.toString()
                val lat = "${place.latLng?.latitude}".toDouble()
                val long = "${place.latLng?.longitude}".toDouble()

                if (address != null) {
                    etPlace.hint = address
                    moveMapToSelectedLocationAndRefresh(lat, long)
                }
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Log.d("onError", status.statusMessage.toString())
            }
        })
    }

    private fun initialisePlacesClient() {
        initializePlacesApi(this)
        placesClient = Places.createClient(this)
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

    private fun searchSessionsInMapArea() {
        val north = mMap.projection.visibleRegion.farLeft.latitude
        val west = mMap.projection.visibleRegion.farLeft.longitude
        val south = mMap.projection.visibleRegion.nearRight.latitude
        val east = mMap.projection.visibleRegion.nearRight.longitude

        val square = GeoSquare(north, south, east, west)
        val sensorInfo = getSensorInfo()

        setupObserverForApiCallWithCoordinatesAndSensor(square, sensorInfo)
    }

    private fun showBottomSheetDialog(session: SessionInRegionResponse) {
        searchFollowViewModel.selectSession(session)
        bottomSheetDialog.show(supportFragmentManager)
    }

    private fun passLatLng() {
        val selectedLat = lat?.toDouble()
        val selectedLng = lng?.toDouble()
        if (selectedLat != null && selectedLng != null) {
            searchFollowViewModel.getLat(selectedLat)
            searchFollowViewModel.getLng(selectedLng)
        }
    }

    private fun getSensorInfo(): SensorInformation {
        return when (txtSensor) {
            ParticulateMatter.AIRBEAM2.getSensorName() -> ParticulateMatter.AIRBEAM2
            ParticulateMatter.OPEN_AQ.getSensorName() -> ParticulateMatter.OPEN_AQ
            ParticulateMatter.PURPLE_AIR.getSensorName() -> ParticulateMatter.PURPLE_AIR
            Ozone.OPEN_AQ.getSensorName() -> Ozone.OPEN_AQ
            else -> ParticulateMatter.AIRBEAM2
        }
    }

    private fun getSensor(): String {
        return when (txtSensor) {
            ParticulateMatter.AIRBEAM2.getSensorName() -> SensorNames.AIRBEAM.getSensorName()
            ParticulateMatter.OPEN_AQ.getSensorName() -> SensorNames.OPEN_AQ.getSensorName()
            ParticulateMatter.PURPLE_AIR.getSensorName() -> SensorNames.PURPLE_AIR.getSensorName()
            Ozone.OPEN_AQ.getSensorName() -> SensorNames.OPEN_AQ.getSensorName()
            else -> SensorNames.AIRBEAM.getSensorName()
        }
    }

    private fun moveMapToSelectedLocationAndRefresh(lat: Double, long: Double) {
        mMap.clear()

        val selectedLocation = LatLng(lat, long)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 10f))

        searchSessionsInMapArea()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        styleGoogleMap(mMap, this)

        val selectedLat = lat?.toDouble()
        val selectedLng = lng?.toDouble()
        if (selectedLat != null && selectedLng != null) {
            val theLocation = LatLng(selectedLat, selectedLng)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(theLocation, 10f))
        }

        mMap.setOnMarkerClickListener(this)
        mMap.setOnCameraMoveStartedListener(this)

        searchSessionsInMapArea()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val uuid = marker.snippet.toString()
        val position = adapter.getSessionPositionBasedOnId(uuid)

        binding.recyclerFixedFollow.scrollToPosition(position)
        adapter.addCardBorder(position)
        return true
    }

    override fun onCameraMoveStarted(p0: Int) {
        binding.btnRedo.visible()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}