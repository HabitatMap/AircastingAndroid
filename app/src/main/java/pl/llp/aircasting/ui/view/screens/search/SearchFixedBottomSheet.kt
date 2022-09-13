package pl.llp.aircasting.ui.view.screens.search

import androidx.core.content.ContextCompat
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
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.databinding.SearchFollowBottomSheetBinding
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.dashboard.charts.BottomSheetChartConfigurator
import pl.llp.aircasting.ui.view.screens.dashboard.charts.Chart
import pl.llp.aircasting.ui.view.screens.session_view.measurement_table_container.MeasurementsTableContainer
import pl.llp.aircasting.ui.view.screens.session_view.measurement_table_container.SessionDetailsMeasurementsTableContainer
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.extensions.*

class SearchFixedBottomSheet : BottomSheet(), OnMapReadyCallback {
    private val searchFollowViewModel: SearchFollowViewModel by activityViewModels()
    private var binding: SearchFollowBottomSheetBinding? = null
    private val options = MarkerOptions()

    private lateinit var txtLat: String
    private lateinit var txtLng: String
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private val mSettings: Settings by lazy {
        (requireActivity().application as AircastingApplication).settings
    }
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
        setupMapFragment()
        setupChart()
        setupFollowButton()
        setupUnfollowButton()
        toggleCorrectButton()
        setupChipsBehaviour()
        setupMeasurementTableLayout()
        showLoader()
    }

    private fun setupMapFragment() {
        mapFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.mapViewBottomSheet) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupChart() {
        mChart = Chart(
            requireContext(),
            binding?.root,
            BottomSheetChartConfigurator(requireContext())
        )
    }

    private fun setupMeasurementTableLayout() {
        mMeasurementsTableContainer = SessionDetailsMeasurementsTableContainer(
            requireContext(),
            this.layoutInflater,
            binding?.root,
            selectable = true,
            displayValues = true
        )
    }

    private fun setupUnfollowButton() {
        binding?.unfollowBtn?.setOnClickListener {
            onUnfollowClicked()

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
            if (userOwnsSession())
                toggleOwnSessionButton() else {
                viewModelScope.launch {
                    if (isSelectedSessionFollowed.await()) toggleUnFollowButton()
                    else toggleFollowButton()
                }
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

    private fun toggleOwnSessionButton() {
        binding?.unfollowBtn?.inVisible()
        binding?.followBtn?.apply {
            visible()
            isEnabled = false
            text = context.getString(R.string.your_session_text)
            setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.aircasting_grey_300
                )
            )
        }
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
        mapFragment.view?.inVisible()
        binding?.chartContainer?.visible()
    }

    private fun toggleMap() {
        mapFragment.view?.visible()
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

                bindSessionPresenter(session, mSensorThresholds, stream)
                bindSelectedStream()
                bindChartData()
                bindSession()
            }
        }
    }

    private fun bindSelectedStream() {
        mSessionPresenter.setStream()
    }

    private fun bindSessionPresenter(
        session: Session,
        mSensorThresholds: HashMap<String, SensorThreshold>,
        stream: MeasurementStream
    ) {
        mSessionPresenter = SessionPresenter(session, mSensorThresholds, stream)
    }

    private fun bindChartData() {
        mChart.bindChart(mSessionPresenter)
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

    private fun bindSession() {
        mMeasurementsTableContainer?.bindSession(
            mSessionPresenter,
            this::onMeasurementStreamChanged
        )
        hideLoader()
    }

    private fun onMeasurementStreamChanged(measurementStream: MeasurementStream) {
        mSessionPresenter.select(measurementStream)
        bindChartData()
    }

    private fun onFollowClicked() {
        searchFollowViewModel.follow()
    }

    private fun onUnfollowClicked() {
        searchFollowViewModel.unfollow()
    }

    private fun showLoader() {
        binding?.loader?.startAnimation()
    }

    private fun hideLoader() {
        binding?.loader?.stopAnimation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMapType(mSettings, requireContext())

        val selectedLat = txtLat.toDouble()
        val selectedLng = txtLng.toDouble()

        val myLocation = LatLng(selectedLat, selectedLng)
        mMap.drawMarkerOnMap(requireActivity(), options, selectedLat, selectedLng, null)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14f))
    }

    override fun onDestroyView() {
        super.onDestroyView()

        requireActivity().supportFragmentManager.beginTransaction().remove(mapFragment)
            .commitAllowingStateLoss()
    }
}