package pl.llp.aircasting.ui.view.screens.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

class SearchFixedSessionsActivity : AppCompatActivity() {

    companion object {
        fun start(rootActivity: FragmentActivity?) {
            rootActivity ?: return

            val intent = Intent(rootActivity, SearchFixedSessionsActivity::class.java)
            rootActivity.startActivity(intent)
        }
    }

    var placesClient: PlacesClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_fixed_sessions)

        setupUI()
        setupAutoComplete()
    }

    private fun setupUI() {
        setSupportActionBar(topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



    }

    private fun setupAutoComplete() {
        if (!Places.isInitialized()) Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)
        placesClient = Places.createClient(this)

        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as AutocompleteSupportFragment?
        autocompleteFragment?.view?.findViewById<EditText>(R.id.places_autocomplete_search_input)?.apply {
            setText(getString(R.string.search_session_query_hint))
            textSize = 15.0f
            setTextColor(ContextCompat.getColor(context, R.color.aircasting_grey_300))
        }
        autocompleteFragment?.view?.findViewById<ImageButton>(R.id.places_autocomplete_search_button)?.visibility = View.GONE

        autocompleteFragment?.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
        )

        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val address = place.address?.toString()

                val lat = "${place.latLng?.latitude!!}::${place.latLng?.longitude!!}"

                val resultIntent = Intent()

                resultIntent.putExtra("location", address)
                resultIntent.putExtra("latlong", lat)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }

            override fun onError(status: Status) {
               Log.d("tag", status.statusMessage.toString())
            }
        })

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}