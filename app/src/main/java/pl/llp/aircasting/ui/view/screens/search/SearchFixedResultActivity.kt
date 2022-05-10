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
import pl.llp.aircasting.databinding.ActivitySearchFollowResultBinding
import pl.llp.aircasting.ui.view.adapters.FixedFollowAdapter
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import pl.llp.aircasting.util.Status.*
import pl.llp.aircasting.util.gone
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
    private val bottomSheetDialog: SearchFixedBottomSheet = SearchFixedBottomSheet()

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

        val jsonData =
            "{\"time_from\":\"1531008000\",\"time_to\":\"1562630399\",\"tags\":\"\",\"usernames\":\"\",\"west\":-73.9766655034307,\"east\":-73.97618605856928,\"south\":40.68019783151002,\"north\":40.680367168382396,\"sensor_name\":\"airbeam2-pm2.5\",\"unit_symbol\":\"µg/m³\",\"measurement_type\":\"Particulate Matter\"}"

        setupRecyclerView()
        setupObserver(jsonData)
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