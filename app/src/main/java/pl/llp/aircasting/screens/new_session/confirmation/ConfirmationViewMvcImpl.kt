package pl.llp.aircasting.screens.new_session.confirmation

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.FragmentManager
import com.google.android.libraries.maps.*
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.BitmapHelper
import pl.llp.aircasting.lib.styleGoogleMap
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.common.BaseObservableViewMvc


abstract class ConfirmationViewMvcImpl: BaseObservableViewMvc<ConfirmationViewMvc.Listener>, ConfirmationViewMvc,
    OnMapReadyCallback {
    protected var session: Session? = null
    protected val areMapsDisabled: Boolean

    private val DEFAULT_ZOOM = 16f

    private var mMarker: Marker? = null
    private var mMap: GoogleMap? = null
    private var mMapFragment: SupportMapFragment? = null
    private var mSupportFragmentManager: FragmentManager?

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?,
        session: Session,
        areMapsDisabled: Boolean
    ): super() {
        this.rootView = inflater.inflate(layoutId(), parent, false)
        this.session = session
        this.mSupportFragmentManager = supportFragmentManager
        this.areMapsDisabled = areMapsDisabled

        val sessionDescription = rootView?.findViewById<TextView>(R.id.description)
        sessionDescription?.text = buildDescription()

        initMap(mSupportFragmentManager)

        val startRecordingButton = rootView?.findViewById<Button>(R.id.start_recording_button)
        startRecordingButton?.setOnClickListener {
            onStartRecordingClicked()
        }
    }

    abstract fun layoutId(): Int
    abstract fun shouldInitMap(): Boolean

    override fun onDestroy() {
        mMap = null
        mMapFragment?.onDestroy()
        mMapFragment?.let {
            mSupportFragmentManager?.beginTransaction()?.remove(it)?.commitAllowingStateLoss()
        }
        mMapFragment = null
    }

    private fun initMap(supportFragmentManager: FragmentManager?) {
        if (shouldInitMap()) {
            mMapFragment = SupportMapFragment.newInstance(mapOptions())
            mMapFragment?.let {
                supportFragmentManager?.beginTransaction()?.replace(R.id.map, it)?.commit()
            }
            mMapFragment?.getMapAsync(this)
        } else {
            val instructions = rootView?.findViewById<TextView>(R.id.instructions)
            instructions?.visibility = View.GONE

            val map = rootView?.findViewById<View>(R.id.map)
            map?.visibility = View.GONE
        }
    }

    protected fun updateMarkerPosition(latitude: Double?, longitude: Double?) {
        if (latitude == null || longitude == null) return

        val location = LatLng(latitude, longitude)
        val zoom = mMap?.cameraPosition?.zoom ?: DEFAULT_ZOOM
        mMarker?.position = location
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap ?: return
        mMap = googleMap
        styleGoogleMap(mMap!!, context)
        val sessionLocation = session?.location ?: return
        val location = LatLng(sessionLocation.latitude, sessionLocation.longitude)
        val icon = BitmapHelper.bitmapFromVector(context, R.drawable.ic_dot_20)
        val marker = MarkerOptions()
            .position(location)
            .icon(icon)
        mMarker = googleMap.addMarker(marker)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM))
    }

    private fun onStartRecordingClicked() {
        for (listener in listeners) {
            listener.onStartRecordingClicked(session!!)
        }
    }

    private fun buildDescription(): SpannableStringBuilder {
        val blueColor = ResourcesCompat.getColor(context.resources, R.color.aircasting_blue_400, null)

        return SpannableStringBuilder()
            .append(getString(R.string.session_confirmation_description_part1))
            .append(" ")
            .color(blueColor, { bold { append(session?.displayedType) } })
            .append(" ")
            .append(getString(R.string.session_confirmation_description_part2))
            .append(" ")
            .color(blueColor, { bold { append(session?.name) } })
            .append(" ")
            .append(getString(R.string.session_confirmation_description_part3))
    }

    private fun mapOptions(): GoogleMapOptions {
        val mapOptions = GoogleMapOptions()
        mapOptions.useViewLifecycleInFragment(true)
        mapOptions.zoomControlsEnabled(true)
        mapOptions.zoomGesturesEnabled(true)
        mapOptions.tiltGesturesEnabled(false)
        mapOptions.scrollGesturesEnabled(false)
        mapOptions.rotateGesturesEnabled(false)

        return mapOptions
    }
}
