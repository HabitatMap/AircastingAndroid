package pl.llp.aircasting.ui.view.screens.search

import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import pl.llp.aircasting.R
import pl.llp.aircasting.data.local.entity.ExtSessionsDBObject
import pl.llp.aircasting.databinding.SearchFollowBottomSheetBinding
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import pl.llp.aircasting.util.*
import kotlin.math.roundToInt

class SearchFixedBottomSheet : BottomSheet(), OnMapReadyCallback {
    private val searchFollowViewModel: SearchFollowViewModel by activityViewModels()
    private var binding: SearchFollowBottomSheetBinding? = null

    private var mapFragment: SupportMapFragment? = null
    private lateinit var mMap: GoogleMap
    private val options = MarkerOptions()
    private var txtLat: Double? = null
    private var txtLng: Double? = null
    private lateinit var loader: AnimatedLoader

    override fun layoutId(): Int {
        return R.layout.search_follow_bottom_sheet
    }

    override fun setup() {
        super.setup()
        binding = contentView?.let { SearchFollowBottomSheetBinding.bind(it) }
        binding?.model = searchFollowViewModel

        setupUI()
        getLatlngObserver()
        observeLastMeasurementsValue()
    }

    private fun setupUI() {
        mapFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.mapViewBottomSheet) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        binding?.followBtn?.setOnClickListener {
            val selectedSession = searchFollowViewModel.selectedSession.value
            if (selectedSession != null) onFollowClicked(ExtSessionsDBObject(selectedSession))

            it.context.showToast(getString(R.string.session_followed))
            it.gone()
            binding?.unfollowBtn?.visible()
        }
        binding?.unfollowBtn?.setOnClickListener {
            val selectedSession = searchFollowViewModel.selectedSession.value
            if (selectedSession != null) onUnfollowClicked(ExtSessionsDBObject(selectedSession))

            it.context.showToast(getString(R.string.session_unfollowed))
            it.gone()
            binding?.followBtn?.visible()
        }

        val loaderImage =
            binding?.measurementsTableBinding?.streamMeasurementHeaderAndValue?.loaderImage as ImageView
        loader = AnimatedLoader(loaderImage)
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

    private fun observeLastMeasurementsValue() {
        val sessionId = searchFollowViewModel.selectedSession.value?.id?.toLong()
        val sensorName = searchFollowViewModel.selectedSession.value?.streams?.sensor?.sensorName
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

    private fun onFollowClicked(extSession: ExtSessionsDBObject) {
        searchFollowViewModel.onFollowSessionClicked(extSession)
    }

    private fun onUnfollowClicked(extSession: ExtSessionsDBObject) {
        searchFollowViewModel.onUnfollowSessionClicked(extSession)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        styleGoogleMap(mMap, requireActivity())

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