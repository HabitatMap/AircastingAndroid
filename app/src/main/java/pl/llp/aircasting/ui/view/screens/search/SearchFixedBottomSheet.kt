package pl.llp.aircasting.ui.view.screens.search

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
import pl.llp.aircasting.ui.view.screens.session_view.measurement_table_container.MeasurementsTableContainer
import pl.llp.aircasting.ui.view.screens.session_view.measurement_table_container.SessionDetailsMeasurementsTableContainer
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import pl.llp.aircasting.util.*

class SearchFixedBottomSheet : BottomSheet(), OnMapReadyCallback {
    private val searchFollowViewModel: SearchFollowViewModel by activityViewModels()
    private var binding: SearchFollowBottomSheetBinding? = null
    private var mapFragment: SupportMapFragment? = null
    private val options = MarkerOptions()

    private lateinit var txtLat: String
    private lateinit var txtLng: String
    private lateinit var mMap: GoogleMap

    private lateinit var mChart: Chart
    private lateinit var mSessionPresenter: SessionPresenter
    private var mMeasurementsTableContainer: MeasurementsTableContainer? = null

    private var mSensorThresholds = hashMapOf<String, SensorThreshold>()

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
        setupMeasurementTableLayout()
    }

    private fun setupMeasurementTableLayout() {
        mMeasurementsTableContainer = SessionDetailsMeasurementsTableContainer(
            requireActivity(),
            this.layoutInflater,
            binding?.root,
            selectable = true,
            displayValues = true
        )
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
        searchFollowViewModel.apply {
            viewModelScope.launch {
                if (isSelectedSessionFollowed.await())
                    toggleUnFollowButton()
                else
                    toggleFollowButton()
            }
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
            myLat.observe(requireActivity()) { mLat ->
                txtLat = mLat.toString()
            }
            myLng.observe(requireActivity()) { mLng ->
                txtLng = mLng.toString()
            }
        }
    }

    private fun getSessionWithAllData() {
        searchFollowViewModel.getStreams().observe(this) { session ->
            val streams = session?.streams
            streams?.map { stream ->
                mSensorThresholds[stream.sensorName] = getSensorThresholds(stream)
                bindChartData(session, mSensorThresholds, stream)
            }
        }
    }

    private fun getSensorThresholds(stream: MeasurementStream): SensorThreshold {
        return SensorThreshold(
            stream.sensorName,
            stream.thresholdVeryLow,
            stream.thresholdLow,
            stream.thresholdMedium,
            stream.thresholdHigh,
            stream.thresholdVeryHigh
        )
    }

    private fun bindChartData(
        session: Session,
        sensorThresholds: HashMap<String, SensorThreshold>,
        selectedStream: MeasurementStream
    ) {
        mSessionPresenter = SessionPresenter(session, sensorThresholds, selectedStream)

        bindSession()
        mChart.bindChart(mSessionPresenter)
    }

    private fun bindSession() {
        mMeasurementsTableContainer?.bindSession(
            mSessionPresenter,
            this::onMeasurementStreamChanged
        )
    }

    private fun onMeasurementStreamChanged(measurementStream: MeasurementStream) {
        mSessionPresenter.selectedStream = measurementStream
        bindSession()
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

        val selectedLat = txtLat.toDouble()
        val selectedLng = txtLng.toDouble()

        val myLocation = LatLng(selectedLat, selectedLng)
        mMap.drawMarkerOnMap(requireActivity(), options, selectedLat, selectedLng, null)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14f))
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mapFragment?.let { parentFragmentManager.beginTransaction().remove(it).commit() }
    }
}