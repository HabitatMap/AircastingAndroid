package pl.llp.aircasting.ui.view.screens.search

import android.annotation.SuppressLint
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
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.responses.search.Session
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

    private lateinit var binding: ActivitySearchFollowResultBinding
    private lateinit var adapter: FixedFollowAdapter
    private val bottomSheetDialog: SearchFixedBottomSheet by lazy { SearchFixedBottomSheet() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_follow_result)

        (application as AircastingApplication)
            .appComponent.inject(this)
        searchFollowViewModel =
            ViewModelProvider(this, viewModelFactory)[SearchFollowViewModel::class.java]

        setupUI()
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        setSupportActionBar(topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val txtParameter = intent.getStringExtra("txtParameter")
        val txtSensor = intent.getStringExtra("txtSensor")

        binding.txtShowing.text = getString(R.string.showing_results_for) + " " + txtParameter
        binding.txtUsing.text = getString(R.string.using_txt) + " " + txtSensor

        val square =
            GeoSquare(40.680367168382396, 40.68019783151002, -73.97618605856928, -73.9766655034307)

        setupRecyclerView()
        setupObserver(square, ParticulateMatter.AIRBEAM)
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
                    it.data?.sessions?.let { it1 -> renderData(it1) }
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

    private fun showBottomSheetDialog(sessions: Session) {
        bottomSheetDialog.show(supportFragmentManager)
    }

    override fun onMapReady(mMap: GoogleMap) {
        val lat = intent.getStringExtra("lat")?.toDouble()
        val long = intent.getStringExtra("long")?.toDouble()

        styleGoogleMap(mMap, this)

        if (lat != null && long != null) {
            val myLocation = LatLng(lat, long)
            mMap.addMarker(
                MarkerOptions()
                    .position(myLocation)
            )
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10f), 1000, null)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}