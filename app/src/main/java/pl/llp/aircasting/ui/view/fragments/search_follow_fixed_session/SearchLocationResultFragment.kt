package pl.llp.aircasting.ui.view.fragments.search_follow_fixed_session

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.Status
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
import kotlinx.android.synthetic.main.app_bar.view.*
import kotlinx.android.synthetic.main.fragment_search_location_result.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.response.search.SessionInRegionResponse
import pl.llp.aircasting.data.api.response.search.SessionsInRegionsResponse
import pl.llp.aircasting.data.api.util.NitrogenDioxide
import pl.llp.aircasting.data.api.util.Ozone
import pl.llp.aircasting.data.api.util.ParticulateMatter
import pl.llp.aircasting.data.api.util.SensorInformation
import pl.llp.aircasting.data.api.util.StringConstants
import pl.llp.aircasting.data.model.GeoSquare
import pl.llp.aircasting.databinding.FragmentSearchLocationResultBinding
import pl.llp.aircasting.ui.view.adapters.FixedFollowAdapter
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.ui.view.screens.search.SearchFixedBottomSheet
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import pl.llp.aircasting.util.Resource
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.Status.*
import pl.llp.aircasting.util.extensions.*
import javax.inject.Inject

class SearchLocationResultFragment @Inject constructor(
    factory: ViewModelProvider.Factory,
    private val mSettings: Settings
) : Fragment(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraMoveStartedListener {

    private var _binding: FragmentSearchLocationResultBinding? = null
    private val binding get() = _binding!!

    private val searchFollowViewModel by activityViewModels<SearchFollowViewModel>(factoryProducer = { factory })

    private lateinit var adapter: FixedFollowAdapter

    private lateinit var mMap: GoogleMap
    private var placesClient: PlacesClient? = null
    private val bottomSheetDialog: SearchFixedBottomSheet by lazy { SearchFixedBottomSheet() }

    private lateinit var address: String
    private lateinit var mLat: String
    private lateinit var mLng: String
    private var mSelectedMarker: Marker? = null
    private val mMarkerArray: ArrayList<Marker> = arrayListOf()

    private val options = MarkerOptions()
    private var txtParameter: String? = null
    private var txtSensor: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity?.application as AircastingApplication)
            .userDependentComponent?.inject(this)
        _binding = FragmentSearchLocationResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {
        setupMapView()

        handleBackButtonPress()

        getArgumentsFromLocationFragment()

        binding.include.finishSearchButton.visible()

        binding.txtShowing.text = getString(R.string.showing_results_for) + " " + txtParameter
        binding.txtUsing.text = getString(R.string.using_txt) + " " + getSensor()

        binding.btnRedo.setOnClickListener { resetTheSearch() }

        binding.include.topAppBar.setNavigationOnClickListener { goToPreviousFragment() }

        setupRecyclerView()
        setupSearchLayout()
        binding.include.finishSearchButton.setOnClickListener { goToDashboard() }
    }

    private fun goToPreviousFragment() {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(
                R.id.fragmentContainer,
                SearchLocationFragment::class.java,
                null,
                "searchLocation"
            )
            ?.disallowAddToBackStack()
            ?.commit()
    }

    private fun setupMapView() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun getArgumentsFromLocationFragment() {
        this.arguments?.let {
            address = it.getString("address").toString()

            mLat = it.getString("lat").toString()
            mLng = it.getString("lng").toString()

            txtParameter = it.getString("txtParameter")
            txtSensor = it.getString("txtSensor")
        }
    }

    private fun setupSearchLayout() {
        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.place_autocomplete_results) as AutocompleteSupportFragment
        autocompleteFragment.apply {
            view?.findViewById<ImageButton>(R.id.places_autocomplete_search_button)?.gone()

            val placeTextInput =
                view?.findViewById<EditText>(R.id.places_autocomplete_search_input)
            placeTextInput?.apply {
                hint = address
                setHintStyle(R.color.aircasting_grey_300)
            }

            initialisePlacesClient()

            setPlaceFields(listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG))

            setupOnPlaceSelectedListener(placeTextInput)
        }
    }

    private fun initialisePlacesClient() {
        initializePlacesApi(requireContext())
        placesClient = Places.createClient(requireContext())
    }

    private fun AutocompleteSupportFragment.setupOnPlaceSelectedListener(placeTextInput: EditText?) {
        setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                address = place.address as String
                placeTextInput?.hint = address
                val lat = place.latLng?.latitude
                val lng = place.latLng?.longitude

                if (lat != null && lng != null) {
                    moveMapToSelectedLocationAndRefresh(lat, lng)
                }
            }

            override fun onError(status: Status) {
                Log.d("onError", status.statusMessage.toString())
            }
        })
    }

    private fun goToDashboard() {
        MainActivity.navigate(requireActivity())
    }

    private fun setupRecyclerView() {
        adapter = FixedFollowAdapter(this::onSessionCardClick)
        binding.recyclerFixedFollow.adapter = adapter
    }

    private fun setupObserverForApiCallWithCoordinatesAndSensor(
        square: GeoSquare,
        sensorInfo: SensorInformation
    ) {
        searchFollowViewModel.getSessionsInRegion(square, sensorInfo).observe(viewLifecycleOwner) {
            when (it.status) {
                SUCCESS -> updateUI(it)
                ERROR -> {
                    stopLoader()
                    activity?.showToast(it.message.toString())
                }
                LOADING -> showLoader()
            }
        }
    }

    private fun showLoader() {
        binding.searchLoader.startAnimation()
    }

    private fun stopLoader() {
        binding.searchLoader.stopAnimation()
    }

    private fun updateUI(it: Resource<SessionsInRegionsResponse>) {
        binding.apply {
            stopLoader()
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

    @SuppressLint("StringFormatMatches")
    private fun updateText(count: Int) {
        val txtShowing = binding.txtShowingSessionsNumber

        if (count != 0) txtShowing.text = HtmlCompat.fromHtml(
            getString(R.string.txt_showing_sessions_number, count, count),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        else txtShowing.apply {
            text = HtmlCompat.fromHtml(
                getString(R.string.txt_showing_sessions_number, count, count),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            setMargins(bottom = 50)
        }
    }

    private fun setupMapMarkers(sessions: List<SessionInRegionResponse>) {
        for (i in sessions.indices) {
            val getLats = sessions[i].latitude
            val getLngs = sessions[i].longitude
            val sessionUUID = sessions[i].uuid
            val markers =
                mMap.drawMarkerOnMap(requireContext(), options, getLats, getLngs, sessionUUID)

            markers?.let { mMarkerArray.add(it) }
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

    private fun onSessionCardClick(session: SessionInRegionResponse, sessionUUID: String) {
        setViewModelLatLng(session)
        highlightTheSelectedDot(sessionUUID)

        searchFollowViewModel.selectSession(session)
        bottomSheetDialog.show(requireActivity().supportFragmentManager)
    }

    private fun setViewModelLatLng(session: SessionInRegionResponse) {
        searchFollowViewModel.setLat(session.latitude)
        searchFollowViewModel.setLng(session.longitude)
    }

    private fun getSensorInfo(): SensorInformation {
        return when (txtSensor) {
            StringConstants.airbeamSensorName -> ParticulateMatter.AIRBEAM
            StringConstants.governmentSensorNamePM -> ParticulateMatter.GOVERNMENT
            StringConstants.governmentSensorNameOzone -> Ozone.GOVERNMENT
            StringConstants.governmentSensorNameNitrogenDioxide -> NitrogenDioxide.GOVERNMENT
            else -> ParticulateMatter.AIRBEAM
        }
    }

    private fun getSensor(): String {
        return when (txtSensor) {
            StringConstants.airbeamSensorName -> StringConstants.airbeam
            StringConstants.governmentSensorNamePM -> StringConstants.govt
            StringConstants.governmentSensorNameOzone -> StringConstants.govt
            StringConstants.governmentSensorNameNitrogenDioxide -> StringConstants.govt
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
        marker.setIcon(
            getBitmapDescriptorFromVector(
                requireContext(),
                R.drawable.map_dot_with_circle_inside
            )
        )
    }

    private fun highlightMarkerIcon(marker: Marker) {
        marker.setIcon(getBitmapDescriptorFromVector(requireContext(), R.drawable.map_dot_selected))
    }

    private fun selectCorrespondingCardView(marker: Marker) {
        val uuid = marker.snippet.toString()
        val position = adapter.getSessionPositionBasedOnId(uuid)

        binding.recyclerFixedFollow.scrollToPosition(position)
        adapter.scrollToSelectedCard(position)
    }

    private fun highlightTheSelectedDot(sessionUUID: String) {
        for (i in mMarkerArray.indices) {
            val marker = mMarkerArray[i]

            if (sessionUUID == marker.snippet)
                highlightMarkerIcon(marker)
            else setMarkerIconToDefault(marker)
        }

        mSelectedMarker = mMarkerArray.find { it.snippet == sessionUUID }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setMinZoomPreference(6f)

        mMap.setMapType(mSettings, requireContext())
        setTextStyleBasedOnSatelliteSettings()

        val lat = mLat.toDouble()
        val lng = mLng.toDouble()

        val theLocation = LatLng(lat, lng)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(theLocation, 10f))

        searchSessionsInMapArea()

        mMap.setOnMarkerClickListener(this)
        mMap.setOnCameraMoveStartedListener(this)
    }

    private fun setTextStyleBasedOnSatelliteSettings() {
        if (mSettings.isUsingSatelliteView()) binding.txtShowingSessionsNumber.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.background_main
            )
        )
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (mSelectedMarker != null) {
            mSelectedMarker?.let { setMarkerIconToDefault(it) }
            mSelectedMarker = null
        }
        mSelectedMarker = marker
        highlightMarkerIcon(marker)

        selectCorrespondingCardView(marker)
        return true
    }

    override fun onCameraMoveStarted(p0: Int) {
        binding.btnRedo.visible()
    }

    private fun handleBackButtonPress() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            if (isEnabled) {
                isEnabled = false
                goToPreviousFragment()
            }
        }
    }
}