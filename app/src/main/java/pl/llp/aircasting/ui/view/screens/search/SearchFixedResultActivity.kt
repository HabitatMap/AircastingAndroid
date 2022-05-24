package pl.llp.aircasting.ui.view.screens.search

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
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
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.BuildConfig
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.response.search.Session
import pl.llp.aircasting.data.api.util.Ozone
import pl.llp.aircasting.data.api.util.ParticulateMatter
import pl.llp.aircasting.data.api.util.SensorInformation
import pl.llp.aircasting.data.model.GeoSquare
import pl.llp.aircasting.databinding.ActivitySearchFollowResultBinding
import pl.llp.aircasting.ui.view.adapters.FixedFollowAdapter
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import pl.llp.aircasting.util.*
import javax.inject.Inject

class SearchFixedResultActivity : AppCompatActivity(), OnMapReadyCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var searchFollowViewModel: SearchFollowViewModel
    private lateinit var adapter: FixedFollowAdapter

    private lateinit var binding: ActivitySearchFollowResultBinding
    private lateinit var mMap: GoogleMap

    private val bottomSheetDialog: SearchFixedBottomSheet by lazy { SearchFixedBottomSheet() }
    private val markerList = ArrayList<LatLng>()
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
        passLatLng()
        getSelectedAreaObserver()
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

    /** I'm gonna need the api key - I couldn't enable it because of sanctions
     ** TODO considering this approach as Plan B because I couldn't figure it out how to refresh the map - there may be a better solution for it
     *  - I was getting Geocoding parameters based on the setOnCameraIdleListener() callback method which works after map has been drawn
     */

    private fun getSelectedAreaObserver() {
        val address = intent.getStringExtra("address").toString()
        searchFollowViewModel.getReversedGeocodingFromGoogleApi(
            address,
            BuildConfig.PLACES_API_KEY
        ).observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.results?.let { results ->
                        results.forEach { data ->
                            data.geometry.viewport.apply {
                                val farLeftLat = northeast.lat
                                val farLeftLong = northeast.lng
                                val nearRightLat = southwest.lat
                                val nearRightLong = southwest.lng

                                getMapVisibleArea(
                                    farLeftLat,
                                    farLeftLong,
                                    nearRightLat,
                                    nearRightLong
                                )
                            }
                        }
                    }
                    binding.progressBar.inVisible()
                }
                Status.ERROR -> {
                    binding.progressBar.inVisible()
                    showToast(it.message.toString())
                }
                Status.LOADING -> binding.progressBar.visible()
            }
        }
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
                    it.data?.sessions?.let { sessions ->
                        sessions.forEach { latLng ->
                            val getLats = latLng.latitude
                            val getLngs = latLng.longitude
                            markerList.add(LatLng(getLats, getLngs))
                        }
                        renderData(sessions.reversed())
                    }
                    binding.progressBar.inVisible()
                }
                Status.ERROR -> {
                    binding.progressBar.inVisible()
                    showToast(it.message.toString())
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

    private fun passLatLng() {
        val lat = intent.getStringExtra("lat")?.toDouble()
        val lng = intent.getStringExtra("long")?.toDouble()
        if (lat != null && lng != null) {
            searchFollowViewModel.getLat(lat)
            searchFollowViewModel.getLng(lng)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val lat = intent.getStringExtra("lat")?.toDouble()
        val long = intent.getStringExtra("long")?.toDouble()

        styleGoogleMap(mMap, this)

        //dummy data for showing the items/cards in recyclerView
        val farLeftLat = 50.35515688978048
        val farLeftLong = 19.675315394997597
        val nearRightLat = 49.77237328564033
        val nearRightLong = 20.214644260704517

        getMapVisibleArea(
            farLeftLat,
            farLeftLong,
            nearRightLat,
            nearRightLong
        )

        if (lat != null && long != null) {
            markerList.forEach { markerData ->
                mMap.addMarker(
                    options
                        .position(LatLng(markerData.latitude, markerData.longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(bitmapDescriptorFromVector(this, R.drawable.ic_dot_20))
                )
            }

            // The selected location
            val theLocation = LatLng(lat, long)
            mMap.addMarker(options.position(theLocation))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(theLocation, 5f))
        }
    }

    private fun getMapVisibleArea(
        farLeftLat: Double,
        farLeftLong: Double,
        nearRightLat: Double,
        nearRightLong: Double
    ) {
        val square = GeoSquare(farLeftLat, nearRightLong, nearRightLat, farLeftLong)
        var sensorInfo: SensorInformation? = null

        when (txtSensor) {
            "airbeam2-pm2.5" -> sensorInfo = ParticulateMatter.AIRBEAM
            "openaq-pm2.5" -> sensorInfo = ParticulateMatter.OPEN_AQ
            "purpleair-pm2.5" -> sensorInfo = ParticulateMatter.PURPLE_AIR
            "openaq-o3" -> sensorInfo = Ozone.OPEN_AQ
        }
        sensorInfo?.let { setupObserver(square, it) }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}