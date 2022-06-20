package pl.llp.aircasting.ui.view.screens.search

import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.response.search.SessionInRegionResponse
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.databinding.SearchFollowBottomSheetBinding
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.dashboard.charts.Chart
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import pl.llp.aircasting.util.*
import kotlin.math.roundToInt

class SearchFixedBottomSheet : BottomSheet(), OnMapReadyCallback {
    private val searchFollowViewModel: SearchFollowViewModel by activityViewModels()
    private var binding: SearchFollowBottomSheetBinding? = null
    private var mapFragment: SupportMapFragment? = null
    private val options = MarkerOptions()
    private var txtLat: Double? = null
    private var txtLng: Double? = null
    private lateinit var loader: AnimatedLoader
    private lateinit var mMap: GoogleMap
    private var mSensorThresholds = hashMapOf<String, SensorThreshold>()
    private lateinit var mChart: Chart
    private lateinit var mSessionPresenter: SessionPresenter

    override fun layoutId(): Int {
        return R.layout.search_follow_bottom_sheet
    }

    override fun setup() {
        super.setup()
        binding = contentView?.let { SearchFollowBottomSheetBinding.bind(it) }
        binding?.model = searchFollowViewModel

        setupUI()
        getLatlngObserver()
        getSessionWithAllData()
        observeLastMeasurementsValue()
    }

    private fun setupUI() {
        mapFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.mapViewBottomSheet) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        mChart = Chart(requireActivity(), binding?.root)

        setupFollowButton()
        setupUnfollowButton()
        toggleCorrectButton()
        setupChipsBehaviour()
        setupLoader()
    }

    private fun setupUnfollowButton() {
        binding?.unfollowBtn?.setOnClickListener {
            val selectedSession = searchFollowViewModel.selectedSession.value
            if (selectedSession != null) onUnfollowClicked(selectedSession)

            it.context.showToast(getString(R.string.session_unfollowed))
            toggleFollowButton()
        }
    }

    private fun setupFollowButton() {
        binding?.followBtn?.setOnClickListener {
            onFollowClicked()

            it.context.showToast(getString(R.string.session_followed))
            toggleUnFollowButton()
        }
    }

    private fun toggleCorrectButton() {
        searchFollowViewModel.viewModelScope.launch(searchFollowViewModel.mainDispatcher) {
            if (searchFollowViewModel.isSelectedSessionFollowed.await())
                toggleUnFollowButton()
            else
                toggleFollowButton()
        }
    }

    private fun toggleFollowButton() {
        binding?.followBtn?.visible()
        binding?.unfollowBtn?.gone()
    }

    private fun toggleUnFollowButton() {
        binding?.followBtn?.gone()
        binding?.unfollowBtn?.visible()
    }

    private fun setupLoader() {
        val loaderImage =
            binding?.measurementsTableBinding?.streamMeasurementHeaderAndValue?.loaderImage as ImageView
        loader = AnimatedLoader(loaderImage)
    }

    private fun setupChipsBehaviour() {
        binding?.chipGroupType?.setOnCheckedStateChangeListener { chipGroup, _ ->
            if (isChartChipSelected(chipGroup)) toggleChart() else toggleMap()
        }
    }

    private fun isChartChipSelected(chipGroup: ChipGroup): Boolean {
        return chipGroup.checkedChipId == binding?.chartChip?.id
    }

    private fun toggleChart() {
        mapFragment?.view?.inVisible()
        binding?.chartContainer?.visible()
    }

    private fun toggleMap() {
        mapFragment?.view?.visible()
        binding?.chartContainer?.inVisible()
    }

    private fun getLatlngObserver() {
        searchFollowViewModel.apply {
            myLat.observe(requireActivity()) {
                txtLat = it
            }
            myLng.observe(requireActivity()) {
                txtLng = it
            }
        }
    }

    private fun getSessionWithAllData() {
        val sessionInRegionResponse = searchFollowViewModel.selectedSession.value
        searchFollowViewModel.getStreams().observe(this) { response ->
            val streams = response?.streams

            if (sessionInRegionResponse != null && streams != null) {
                val session = Session(sessionInRegionResponse, streams)

                streams.map {
                    val sensorThreshold = SensorThreshold(
                        it.sensorName,
                        it.thresholdVeryLow,
                        it.thresholdLow,
                        it.thresholdMedium,
                        it.thresholdHigh,
                        it.thresholdVeryHigh
                    )

                    mSensorThresholds[it.sensorName] = sensorThreshold
                    bindChartData(session, mSensorThresholds, it)
                }
            }
        }
    }

    private fun bindChartData(
        session: Session,
        sensorThresholds: HashMap<String, SensorThreshold>,
        selectedStream: MeasurementStream
    ) {
        mSessionPresenter = SessionPresenter(session, sensorThresholds, selectedStream)
        mChart.bindChart(mSessionPresenter)
    }

    private fun observeLastMeasurementsValue() {
        val sessionId = searchFollowViewModel.selectedSession.value?.id
        val sensorName =
            searchFollowViewModel.selectedSession.value?.streams?.sensor?.sensorName
        if (sensorName != null && sessionId != null) {
            searchFollowViewModel.getLastStreamFromSelectedSession(sessionId, sensorName)
                .observe(this) {
                    when (it.status) {
                        Status.SUCCESS -> {
                            val value = it.data?.lastMeasurementValue ?: 0.0
                            binding?.lastMeasurement = value.roundToInt().toString()

                            setThresholdColour(value)
                            loader.stop()
                        }
                        Status.LOADING -> loader.start()

                        Status.ERROR -> {
                            loader.stop()
                            context?.showToast(it.message.toString())
                        }
                    }
                }
        }
    }

    private fun setThresholdColour(value: Double) {
        val sensor = searchFollowViewModel.selectedSession.value?.streams?.sensor
        if (sensor != null) {
            searchFollowViewModel.selectColor(
                SensorThresholdColorPicker(value, sensor)
                    .getColor()
            )
        }
    }

    private fun onFollowClicked() {
        searchFollowViewModel.saveSession()
    }

    private fun onUnfollowClicked(session: SessionInRegionResponse) {
        searchFollowViewModel.deleteSession(session)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        styleGoogleMap(mMap, requireActivity())

        mMap.uiSettings.setAllGesturesEnabled(false)

        if (txtLat != null && txtLng != null) {
            val myLocation = LatLng(txtLat!!, txtLng!!)
            mMap.drawMarkerOnMap(requireActivity(), options, txtLat!!, txtLng!!, null)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (mapFragment != null) parentFragmentManager.beginTransaction().remove(mapFragment!!)
            .commit()
    }
}