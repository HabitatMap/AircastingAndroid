package pl.llp.aircasting.ui.view.screens.search

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.R
import pl.llp.aircasting.databinding.ActivitySearchFollowResultBinding
import pl.llp.aircasting.ui.view.adapters.FixedFollowAdapter
import pl.llp.aircasting.util.styleGoogleMap

class SearchFixedResultActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivitySearchFollowResultBinding
    private lateinit var adapter: FixedFollowAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_follow_result)

        setupUI()
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        setSupportActionBar(topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // retrieving data from the previous activity
        val lat = intent.getStringExtra("lat")
        val long = intent.getStringExtra("long")

        binding.txtShowing.text = getString(R.string.showing_results_for) + " " + "particulate matter"
        binding.txtUsing.text = getString(R.string.using_txt) + " " + "OpenAQ"

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = FixedFollowAdapter(arrayListOf())
        binding.recyclerFixedFollow.adapter = adapter
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