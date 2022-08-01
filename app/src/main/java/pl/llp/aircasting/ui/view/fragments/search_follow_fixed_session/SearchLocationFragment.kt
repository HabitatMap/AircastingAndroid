package pl.llp.aircasting.ui.view.fragments.search_follow_fixed_session

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.app_bar.view.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.util.StringConstants
import pl.llp.aircasting.databinding.FragmentSearchFollowLocationBinding
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.extensions.gone
import pl.llp.aircasting.util.extensions.initializePlacesApi
import pl.llp.aircasting.util.extensions.setStyle
import pl.llp.aircasting.util.extensions.visible
import javax.inject.Inject

class SearchLocationFragment : Fragment() {

    private var _binding: FragmentSearchFollowLocationBinding? = null
    private val binding get() = _binding!!

    private var placesClient: PlacesClient? = null
    private var txtSelectedParameter: String = StringConstants.measurementTypePM
    private var txtSelectedSensor: String = StringConstants.openAQsensorNamePM

    @Inject
    lateinit var mSettings: Settings

    @Inject
    lateinit var fragmentFactory: FragmentFactory

    private lateinit var address: String
    private lateinit var mLat: String
    private lateinit var mLng: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)
        activity?.supportFragmentManager?.fragmentFactory = fragmentFactory
        _binding = FragmentSearchFollowLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupAutoComplete()
    }

    private fun setupUI() {
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
            binding.appBarSearch.topAppBar.setNavigationOnClickListener {
                requireActivity().onBackPressed()
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
            childFragmentManager.findFragmentById(R.id.place_autocomplete_fragment)
                    as AutocompleteSupportFragment?

        autocompleteFragment?.apply {
            val editTextInput =
                view?.findViewById<EditText>(R.id.places_autocomplete_search_input)
            view?.findViewById<ImageButton>(R.id.places_autocomplete_search_button)
                ?.gone()

            editTextInput?.setStyle(
                getString(R.string.search_session_query_hint),
                R.color.aircasting_grey_300
            )

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
        initializePlacesApi(requireContext())
        placesClient = Places.createClient(requireContext())
    }

    private fun goToSearchResult() {
        val args = bundleOf(
            "address" to address,
            "txtParameter" to txtSelectedParameter,
            "txtSensor" to txtSelectedSensor,
            "lat" to mLat,
            "lng" to mLng
        )

        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.frameLayout, MapResultFragment::class.java, args)
            ?.commit()
    }
}