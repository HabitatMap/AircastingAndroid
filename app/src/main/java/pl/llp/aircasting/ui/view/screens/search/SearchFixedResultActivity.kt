package pl.llp.aircasting.ui.view.screens.search

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_search_follow_result.*
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.R
import pl.llp.aircasting.util.styleGoogleMap

class SearchFixedResultActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_follow_result)

        setupUI()
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        setSupportActionBar(topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // retrieving data from the previous activity
        val lat = intent.getStringExtra("lat")
        val long = intent.getStringExtra("long")
        txtShowing.text = getString(R.string.showing_results_for) + " " + intent.getStringExtra("txtType")

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
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