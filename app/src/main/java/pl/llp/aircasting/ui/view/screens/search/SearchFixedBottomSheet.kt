package pl.llp.aircasting.ui.view.screens.search

import android.util.Log
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
import pl.llp.aircasting.util.SearchHelper.Companion.formatDate
import pl.llp.aircasting.util.SearchHelper.Companion.formatSensorName
import pl.llp.aircasting.util.SearchHelper.Companion.formatTime
import pl.llp.aircasting.util.SearchHelper.Companion.formatType
import pl.llp.aircasting.util.styleGoogleMap

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
        setObserver()
        getLatlngObserver()
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

    private fun setObserver() {
        viewModel.selectedSession.observe(this) {
            binding?.apply {
                title = it.title
                startDate = formatDate(it.startTimeLocal)
                startTime = formatTime(it.startTimeLocal)
                endDate = formatDate(it.endTimeLocal)
                endTime = formatTime(it.endTimeLocal)
                type = formatType(it.type)
                sensorName = formatSensorName(it.streams.sensor.sensorName)
                header = it.streams.sensor.measurementShortType
                // Testing
                tintColor = R.color.aircasting_pink
                Log.i("Color", tintColor.toString())
                Log.i("Resource", R.color.aircasting_pink.toString())
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