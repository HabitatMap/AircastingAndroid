package pl.llp.aircasting.ui.view.screens.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.BuildConfig
import pl.llp.aircasting.R

class SearchFixedSessionsActivity : AppCompatActivity() {

    companion object {
        fun start(rootActivity: FragmentActivity?) {
            rootActivity ?: return

            val intent = Intent(rootActivity, SearchFixedSessionsActivity::class.java)
            rootActivity.startActivity(intent)
        }
    }

    private var placesClient: PlacesClient? = null
    private var btnContinue: Button? = null
    private var ozonChip: Chip? = null
    private var airbeamChip: Chip? = null
    private var purpleChip: Chip? = null
    private var openAQ: Chip? = null
    private var txtSelectedParameter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_fixed_sessions)

        setupUI()
        setupAutoComplete()
    }

    private fun setupUI() {
        setSupportActionBar(topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnContinue = findViewById(R.id.btnContinue)
        airbeamChip = findViewById(R.id.airbeam_chip)
        purpleChip = findViewById(R.id.purple_air_chip)
        ozonChip = findViewById(R.id.ozone_chip)
        openAQ = findViewById(R.id.open_aq_chip)

        val chipFirstGroup = findViewById<ChipGroup>(R.id.chip_group_first)
        chipFirstGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == ozonChip?.id){
                airbeamChip?.visibility = View.GONE
                purpleChip?.visibility = View.GONE
                openAQ?.isChecked = true
                txtSelectedParameter = "Ozon"

            } else {
                airbeamChip?.visibility = View.VISIBLE
                purpleChip?.visibility = View.VISIBLE
                txtSelectedParameter = "particulate matter"
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
                findViewById<ImageButton>(R.id.places_autocomplete_search_button)?.visibility =
                    View.GONE
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
                    val address = place.address?.toString()
                    lat = "${place.latLng?.latitude}"
                    long = "${place.latLng?.longitude}"

                    if (address != null) {
                        btnContinue?.visibility = View.VISIBLE
                        etPlace.hint = address
                    } else Toast.makeText(
                        this@SearchFixedSessionsActivity,
                        "Something went wrong!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(status: Status) {
                    Toast.makeText(
                        this@SearchFixedSessionsActivity,
                        "Something went wrong!",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("tag", status.statusMessage.toString())
                }
            })

            btnContinue?.setOnClickListener {
                goToSearchResult(lat.toString(), long.toString())
            }
        }
    }

    private fun goToSearchResult(lat: String, long: String) {
        val intent = Intent(this@SearchFixedSessionsActivity, SearchFixedResultActivity::class.java)
        intent.putExtra("lat", lat)
        intent.putExtra("long", long)
        intent.putExtra("txtType", txtSelectedParameter)
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}