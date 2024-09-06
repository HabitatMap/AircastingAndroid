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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.android.synthetic.main.app_bar.view.topAppBar
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.util.StringConstants
import pl.llp.aircasting.databinding.FragmentSearchLocationBinding
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.extensions.gone
import pl.llp.aircasting.util.extensions.initializePlacesApi
import pl.llp.aircasting.util.extensions.setHintStyle
import pl.llp.aircasting.util.extensions.visible
import javax.inject.Inject
/*
* due to openAQ and PurpleAir revoking free access to their API
* option to select them was removed (8th of Dec, 2023)
*/
class SearchLocationFragment @Inject constructor(
    factory: ViewModelProvider.Factory
) : Fragment() {

    private var _binding: FragmentSearchLocationBinding? = null
    private val binding get() = _binding!!
    private val searchFollowViewModel by activityViewModels<SearchFollowViewModel>(
        factoryProducer = { factory }
    )
    private var placesClient: PlacesClient? = null

    @Inject
    lateinit var mSettings: Settings

    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private lateinit var address: String
    private lateinit var mLat: String
    private lateinit var mLng: String
    private lateinit var editTextInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity?.application as AircastingApplication)
            .userDependentComponent?.inject(this)
        _binding = FragmentSearchLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupAutoComplete()
        setAddressInSearchIfExist()
    }

    private fun setupUI() {
        binding.apply {
            binding.appBarSearch.topAppBar.setNavigationOnClickListener { activity?.finish() }
            btnContinue.setOnClickListener { goToSearchResult() }
        }
    }

    private fun setupAutoComplete() {
        initialisePlacesClient()

        autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.place_autocomplete_fragment)
                    as AutocompleteSupportFragment

        autocompleteFragment.apply {
            editTextInput = view?.findViewById(R.id.places_autocomplete_search_input) as EditText
            view?.findViewById<ImageButton>(R.id.places_autocomplete_search_button)?.gone()

            editTextInput.apply {
                hint = getString(R.string.search_session_query_hint)
                setHintStyle(R.color.aircasting_grey_300)
            }

            setPlaceFields(listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG))

            onPlaceSelectedListener(editTextInput)
        }
    }

    private fun AutocompleteSupportFragment.onPlaceSelectedListener(placeTextInput: EditText?) {
        setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                address = place.address as String
                placeTextInput?.hint = address
                mLat = place.latLng?.latitude.toString()
                mLng = place.latLng?.longitude.toString()

                saveInputsToViewModel()

                binding.btnContinue.visible()
            }

            override fun onError(status: Status) {
                Log.d("onError", status.statusMessage.toString())
            }
        })
    }

    private fun setAddressInSearchIfExist() {
        searchFollowViewModel.apply {
            mSavedAddress.observe(viewLifecycleOwner) { mAddress ->
                if (mAddress != null) {

                    editTextInput.hint = mAddress
                    address = mAddress
                    binding.btnContinue.visible()
                }
            }
            myLat.observe(viewLifecycleOwner) { myLat ->
                if (myLat != null) {
                    mLat = myLat.toString()
                }
            }
            myLng.observe(viewLifecycleOwner) { myLng ->
                if (myLng != null) {
                    mLng = myLng.toString()
                }
            }
        }
    }

    private fun saveInputsToViewModel() {
        searchFollowViewModel.apply {
            saveAddressFromSearchFragment(address)
            setLat(mLat.toDouble())
            setLng(mLng.toDouble())
        }
    }

    private fun initialisePlacesClient() {
        initializePlacesApi(requireContext())
        placesClient = Places.createClient(requireContext())
    }

    private fun goToSearchResult() {
        val args = bundleOf(
            "address" to address,
            "txtParameter" to StringConstants.measurementTypePM,
            "txtSensor" to StringConstants.airbeam2sensorName,
            "lat" to mLat,
            "lng" to mLng
        )

        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragmentContainer, SearchLocationResultFragment::class.java, args)
            ?.commit()
    }
}