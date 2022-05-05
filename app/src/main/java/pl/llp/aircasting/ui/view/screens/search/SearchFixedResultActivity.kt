package pl.llp.aircasting.ui.view.screens.search

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.responses.search.Session
import pl.llp.aircasting.databinding.ActivitySearchFollowResultBinding
import pl.llp.aircasting.ui.view.adapters.FixedFollowAdapter
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import pl.llp.aircasting.util.Status.*
import pl.llp.aircasting.util.inVisible
import pl.llp.aircasting.util.styleGoogleMap
import pl.llp.aircasting.util.visible
import javax.inject.Inject

class SearchFixedResultActivity : AppCompatActivity(), OnMapReadyCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var searchFollowViewModel: SearchFollowViewModel

    private lateinit var binding: ActivitySearchFollowResultBinding
    private lateinit var adapter: FixedFollowAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_follow_result)

        (application as AircastingApplication)
            .appComponent.inject(this)
        searchFollowViewModel =
            ViewModelProviders.of(this, viewModelFactory)[SearchFollowViewModel::class.java]

        setupUI()
    }

    private fun setupUI() {
        setSupportActionBar(topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // retrieving data from the previous activity
        val lat = intent.getStringExtra("lat")
        val long = intent.getStringExtra("long")

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        binding.txtShowing.text =
            getString(R.string.showing_results_for) + " " + "particulate matter"
        binding.txtUsing.text = getString(R.string.using_txt) + " " + "OpenAQ"

        setupRecyclerView()

        val jsonData =
            "{\"time_from\":\"1531008000\",\"time_to\":\"1562630399\",\"tags\":\"\",\"usernames\":\"\",\"west\":-73.9766655034307,\"east\":-73.97618605856928,\"south\":40.68019783151002,\"north\":40.680367168382396,\"sensor_name\":\"airbeam2-pm2.5\",\"unit_symbol\":\"µg/m³\",\"measurement_type\":\"Particulate Matter\"}"
        setupObserver(jsonData)
    }

    private fun setupRecyclerView() {
        adapter = FixedFollowAdapter(arrayListOf())
        binding.recyclerFixedFollow.adapter = adapter
    }

    private fun setupObserver(query: String) {
        searchFollowViewModel.getSessionsInRegion(query).observe(this) {
            when (it.status) {
                SUCCESS -> {
                    binding.progressBar.inVisible()
                    it.data?.let { sessions -> renderData(sessions) }
                }
                LOADING -> {
                    binding.progressBar.visible()
                }
                ERROR -> {
                    binding.progressBar.inVisible()
                    Toast.makeText(this, "Error!" + it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun renderData(mySessions: List<Session>) {
        adapter.addData(mySessions)
        adapter.notifyDataSetChanged()
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.search_follow_bottom_sheet)

        bottomSheetDialog.show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        styleGoogleMap(googleMap, this)

        val sydney = LatLng(-33.852, 151.211)
        googleMap.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney")
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}