package pl.llp.aircasting.ui.view.screens.search

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.VisibleRegion
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.responses.search.Session
import pl.llp.aircasting.data.api.util.Ozone
import pl.llp.aircasting.data.api.util.ParticulateMatter
import pl.llp.aircasting.data.api.util.SensorInformation
import pl.llp.aircasting.data.model.GeoSquare
import pl.llp.aircasting.databinding.ActivitySearchFollowResultBinding
import pl.llp.aircasting.ui.view.adapters.FixedFollowAdapter
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import pl.llp.aircasting.util.*
import javax.inject.Inject

class SearchFixedResultActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var searchFollowViewModel: SearchFollowViewModel
    lateinit var adapter: FixedFollowAdapter

    private lateinit var binding: ActivitySearchFollowResultBinding
    private val bottomSheetDialog: SearchFixedBottomSheet by lazy { SearchFixedBottomSheet() }
    private lateinit var mMap: GoogleMap
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

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        txtParameter = intent.getStringExtra("txtParameter")
        txtSensor = intent.getStringExtra("txtSensor")

        binding.txtShowing.text = getString(R.string.showing_results_for) + " " + txtParameter
        binding.txtUsing.text = getString(R.string.using_txt) + " " + txtSensor

        setupRecyclerView()
        setupSearchLayout()
    }

    private fun setupSearchLayout() {
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as AutocompleteSupportFragment?

        autocompleteFragment?.apply {
            view?.apply {
                findViewById<EditText>(R.id.places_autocomplete_search_input)?.apply {
                    setText(intent.getStringExtra("address"))
                    textSize = 15.0f
                    setTextColor(ContextCompat.getColor(context, R.color.aircasting_grey_300))
                }
                findViewById<ImageButton>(R.id.places_autocomplete_search_button)?.gone()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = FixedFollowAdapter(arrayListOf(), this::showBottomSheetDialog)
        binding.recyclerFixedFollow.adapter = adapter
    }

    private fun setupObserver(square: GeoSquare, sensorInfo: SensorInformation) {
        searchFollowViewModel.getSessionsInRegion(square, sensorInfo).observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.sessions?.let { it1 -> renderData(it1.reversed()) }
                    binding.progressBar.inVisible()
                }
                Status.ERROR -> {
                    binding.progressBar.inVisible()
                    Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
                Status.LOADING -> binding.progressBar.visible()
            }
        }
    }

    private fun renderData(mySessions: List<Session>) {
        adapter.addData(mySessions)
        adapter.notifyDataSetChanged()
    }

    private fun showBottomSheetDialog(session: Session) {
        searchFollowViewModel.selectSession(session)
        bottomSheetDialog.show(supportFragmentManager)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val lat = intent.getStringExtra("lat")?.toDouble()
        val long = intent.getStringExtra("long")?.toDouble()

        styleGoogleMap(mMap, this)
        mMap.setOnCameraIdleListener(this)

        if (lat != null && long != null) {
            val myLocation = LatLng(lat, long)
            mMap.addMarker(
                MarkerOptions()
                    .position(myLocation)
            )
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 12f), 1000, null)
        }
    }

    override fun onCameraIdle() {
        getMapVisibleRadius(mMap)
    }

    private fun getMapVisibleRadius(mMap: GoogleMap) {
        val visibleRegion: VisibleRegion = mMap.projection.visibleRegion

        val farLeftLat = visibleRegion.farLeft.latitude // north
        val farLeftLong = visibleRegion.farLeft.longitude // west
        val nearRightLat = visibleRegion.nearRight.latitude // south
        val nearRightLong = visibleRegion.nearRight.longitude // east

        val square = GeoSquare(farLeftLat, nearRightLong, nearRightLat, farLeftLong)
        var sensorInfo: SensorInformation? = null

        when (txtSensor) {
            "airbeam2-pm2.5" -> sensorInfo = ParticulateMatter.AIRBEAM
            "openaq-pm2.5" -> sensorInfo = ParticulateMatter.OPEN_AQ
            "purpleair-pm2.5" -> sensorInfo = ParticulateMatter.PURPLE_AIR
            "openaq-o3" -> sensorInfo = Ozone.OPEN_AQ
        }
        if (sensorInfo != null) setupObserver(square, sensorInfo)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}