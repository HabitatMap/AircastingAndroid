package pl.llp.aircasting.ui.view.screens.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.BuildConfig
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.util.Ozone
import pl.llp.aircasting.data.api.util.ParticulateMatter
import pl.llp.aircasting.databinding.ActivitySearchFixedSessionsBinding
import pl.llp.aircasting.util.gone
import pl.llp.aircasting.util.visible

class SearchFixedSessionsActivity : AppCompatActivity() {

    companion object {
        fun start(rootActivity: FragmentActivity?) {
            rootActivity ?: return

            val intent = Intent(rootActivity, SearchFixedSessionsActivity::class.java)
            rootActivity.startActivity(intent)
        }
    }

    private lateinit var binding: ActivitySearchFixedSessionsBinding
    private var placesClient: PlacesClient? = null
    private var txtSelectedParameter: String? = null
    private var txtSelectedSensor: String? = null
    private var address: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_fixed_sessions)

        setupUI()
        setupAutoComplete()
    }

    private fun setupUI() {
        setSupportActionBar(topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.chipGroupFirstLevel.setOnCheckedStateChangeListener { chipGroup, checkedIds ->
            txtSelectedParameter = if (chipGroup.checkedChipId == binding.ozoneChip.id) {
                binding.airbeamChip.gone()
                binding.purpleAirChip.gone()
                Ozone.OPEN_AQ.getMeasurementType()
            } else {
                binding.airbeamChip.visible()
                binding.purpleAirChip.visible()
                ParticulateMatter.AIRBEAM.getMeasurementType()
            }
        }
        binding.chipGroupSecondLevel.setOnCheckedStateChangeListener { chipGroup, checkedIds ->
            when (chipGroup.checkedChipId) {
                binding.airbeamChip.id -> txtSelectedSensor =
                    ParticulateMatter.AIRBEAM.getSensorName()
                binding.openAqChip.id -> txtSelectedSensor =
                    ParticulateMatter.OPEN_AQ.getSensorName()
                binding.purpleAirChip.id -> txtSelectedSensor =
                    ParticulateMatter.PURPLE_AIR.getSensorName()
            }
        }
    }

    private fun setupAutoComplete() {
        if (!Places.isInitialized()) Places.initialize(
            applicationContext,
            BuildConfig.PLACES_API_KEY
        )
        placesClient = Places.createClient(this)

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as AutocompleteSupportFragment?

        autocompleteFragment?.apply {
            view?.apply {
                findViewById<EditText>(R.id.places_autocomplete_search_input)?.apply {
                    setText(getString(R.string.search_session_query_hint))
                    textSize = 15.0f
                    setTextColor(ContextCompat.getColor(context, R.color.aircasting_grey_300))
                }
                findViewById<ImageButton>(R.id.places_autocomplete_search_button)?.gone()
            }

            setPlaceFields(
                listOf(
                    Place.Field.ID,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
                )
            )

            val etPlace = view?.findViewById(R.id.places_autocomplete_search_input) as EditText

            var lat: String? = null
            var long: String? = null

            setOnPlaceSelectedListener(object : PlaceSelectionListener {
                override fun onPlaceSelected(place: Place) {
                    address = place.address?.toString()
                    lat = "${place.latLng?.latitude}"
                    long = "${place.latLng?.longitude}"

                    if (address != null) {
                        binding.btnContinue.visible()
                        etPlace.hint = address
                    }
                }

                override fun onError(status: Status) {
                    Log.d("onError", status.statusMessage.toString())
                }
            })

            binding.btnContinue.setOnClickListener {
                if (lat != null &&
                    long != null &&
                    txtSelectedParameter != null &&
                    txtSelectedSensor != null
                ) goToSearchResult(
                    lat.toString(),
                    long.toString()
                )
            }
        }
    }

    private fun goToSearchResult(lat: String, long: String) {
        val intent = Intent(this, SearchFixedResultActivity::class.java)
        intent.putExtra("address", address)
        intent.putExtra("txtParameter", txtSelectedParameter)
        intent.putExtra("txtSensor", txtSelectedSensor)

        intent.putExtra("lat", lat)
        intent.putExtra("long", long)

        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}