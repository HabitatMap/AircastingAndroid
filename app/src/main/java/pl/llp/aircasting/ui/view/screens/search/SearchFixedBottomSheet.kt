package pl.llp.aircasting.ui.view.screens.search

import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import pl.llp.aircasting.R
import pl.llp.aircasting.databinding.SearchFollowBottomSheetBinding
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import pl.llp.aircasting.util.Status
import pl.llp.aircasting.util.styleGoogleMap
import kotlin.math.roundToInt

class SearchFixedBottomSheet : BottomSheet(), OnMapReadyCallback {
    private val viewModel: SearchFollowViewModel by activityViewModels()
    private var binding: SearchFollowBottomSheetBinding? = null

    private lateinit var mMap: GoogleMap
    private var txtLat: Double? = null
    private var txtLng: Double? = null

    override fun layoutId(): Int {
        return R.layout.search_follow_bottom_sheet
    }

    override fun setup() {
        super.setup()
        binding = contentView?.let { SearchFollowBottomSheetBinding.bind(it) }

        setupUI()
        getLatlngObserver()
        binding?.model = viewModel
        setLastMeasurementsValue()
    }

    private fun setLastMeasurementsValue() {
        var sessionId = viewModel.selectedSession.value?.id?.toLong()
        var sensorName = viewModel.selectedSession.value?.streams?.sensor?.sensorName
        if (sensorName != null && sessionId != null) {
            // For Tests
            sessionId = 1764780L
            sensorName = "PurpleAir-PM2.5"
            // For Tests
            viewModel.getLastStreamFromSelectedSession(sessionId, sensorName).observe(this) {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding?.lastMeasurement =
                            it.data?.lastMeasurementValue?.roundToInt().toString()
                    }
                    Status.LOADING -> {
                        Toast.makeText(context, "Loading last measurements...", Toast.LENGTH_SHORT)
                            .show()
                    }
                    Status.ERROR -> {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupUI() {
        val mapFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.mapViewBottomSheet) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        binding?.followBtn?.setOnClickListener {
            onSessionFollowClicked()
        }
    }

    private fun onSessionFollowClicked() {
        // todo
    }

    private fun getLatlngObserver() {
        viewModel.apply {
            myLat.observe(requireActivity()) {
                txtLat = it
            }
            myLng.observe(requireActivity()) {
                txtLng = it
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        styleGoogleMap(mMap, requireActivity())

        if (txtLat != null && txtLng != null) {
            val myLocation = LatLng(txtLat!!, txtLng!!)
            mMap.addMarker(
                MarkerOptions()
                    .position(myLocation)
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f))
        }
    }
}