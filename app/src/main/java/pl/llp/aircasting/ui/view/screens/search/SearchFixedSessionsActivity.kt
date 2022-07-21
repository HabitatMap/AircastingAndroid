package pl.llp.aircasting.ui.view.screens.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.util.StringConstants
import pl.llp.aircasting.databinding.ActivitySearchFixedSessionsBinding
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.extensions.gone
import pl.llp.aircasting.util.extensions.initializePlacesApi
import pl.llp.aircasting.util.extensions.setStyle
import pl.llp.aircasting.util.extensions.visible

class SearchFixedSessionsActivity : BaseActivity() {

    companion object {
        fun start(rootActivity: FragmentActivity?) {
            rootActivity ?: return

            val intent = Intent(rootActivity, SearchFixedSessionsActivity::class.java)
            rootActivity.startActivity(intent)
        }
    }

    private lateinit var binding: ActivitySearchFixedSessionsBinding
    private var placesClient: PlacesClient? = null
    private var txtSelectedParameter: String = StringConstants.measurementTypePM
    private var txtSelectedSensor: String = StringConstants.openAQsensorNamePM
    private val mSettings: Settings by lazy { Settings(this.application) }

    private lateinit var address: String
    private lateinit var mLat: String
    private lateinit var mLng: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_fixed_sessions)

        setupUI()
        setupAutoComplete()
    }

    private fun setupUI() {
        setSupportActionBar(topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.apply {
            chipGroupFirstLevel.setOnCheckedStateChangeListener { chipGroup, _ ->
                onFirstChipGroupSelected(
                    chipGroup
                )
            }
            chipGroupSecondLevelOne.setOnCheckedStateChangeListener { chipGroup, _ ->
                onChipGroupSecondLevelSelected(
                    chipGroup
                )
            }
            chipGroupSecondLevelTwo.setOnCheckedStateChangeListener { chipGroup, _ ->
                onChipGroupSecondLevelTwoSelected(
                    chipGroup
                )
            }

            btnContinue.setOnClickListener { goToSearchResult() }
        }
    }

    private fun onFirstChipGroupSelected(chipGroup: ChipGroup) {
        if (chipGroup.checkedChipId == binding.ozoneChip.id) {
            binding.chipGroupSecondLevelOne.gone()
            binding.chipGroupSecondLevelTwo.visible()

            txtSelectedParameter = StringConstants.measurementTypeOzone
            txtSelectedSensor = StringConstants.openAQsensorNameOzone
        } else {
            binding.chipGroupSecondLevelOne.visible()
            binding.chipGroupSecondLevelTwo.gone()

            txtSelectedParameter = StringConstants.measurementTypePM
            txtSelectedSensor = StringConstants.openAQsensorNamePM
        }
    }

    private fun onChipGroupSecondLevelSelected(chipGroup: ChipGroup) {
        txtSelectedParameter = StringConstants.measurementTypePM
        txtSelectedSensor = when (chipGroup.checkedChipId) {
            binding.airbeamChip.id -> StringConstants.airbeam2sensorName
            binding.openAQFirstChip.id -> StringConstants.openAQsensorNamePM
            binding.purpleAirChip.id -> StringConstants.purpleAirSensorName
            else -> StringConstants.openAQsensorNamePM
        }
    }

    private fun onChipGroupSecondLevelTwoSelected(chipGroup: ChipGroup) {
        if (chipGroup.checkedChipId == binding.openAQSecondChip.id) {
            txtSelectedParameter = StringConstants.measurementTypeOzone
            txtSelectedSensor = StringConstants.openAQsensorNameOzone
        }
    }

    private fun setupAutoComplete() {
        initialisePlacesClient()

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as AutocompleteSupportFragment?

        autocompleteFragment?.apply {
            val editTextInput =
                view?.findViewById<EditText>(R.id.places_autocomplete_search_input)
            findViewById<ImageButton>(R.id.places_autocomplete_search_button)?.gone()

            editTextInput?.setStyle(getString(R.string.search_session_query_hint), R.color.aircasting_grey_300)

            setPlaceFields(listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG))

            onPlaceSelectedListener(editTextInput)
        }
    }

    private fun AutocompleteSupportFragment.onPlaceSelectedListener(etPlace: EditText?) {
        setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                address = place.address as String
                mLat = place.latLng?.latitude.toString()
                mLng = place.latLng?.longitude.toString()

                setTextColor()
                binding.btnContinue.visible()
            }

            private fun setTextColor() {
                if (mSettings.isDarkThemeEnabled()) etPlace?.setStyle(
                    address,
                    R.color.aircasting_white
                ) else etPlace?.setStyle(address, R.color.black_color)
            }

            override fun onError(status: Status) {
                Log.d("onError", status.statusMessage.toString())
            }
        })
    }

    private fun initialisePlacesClient() {
        initializePlacesApi(this)
        placesClient = Places.createClient(this)
    }

    private fun goToSearchResult() {
        val intent = Intent(this, SearchFixedResultActivity::class.java)
        intent.putExtra("address", address)
        intent.putExtra("txtParameter", txtSelectedParameter)
        intent.putExtra("txtSensor", txtSelectedSensor)

        intent.putExtra("lat", mLat)
        intent.putExtra("lng", mLng)

        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}