package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.FragmentManager
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.*
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.BitmapHelper
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.Session


abstract class ConfirmationViewMvcImpl: BaseObservableViewMvc<ConfirmationViewMvc.Listener>, ConfirmationViewMvc,
    OnMapReadyCallback {
    private var session: Session? = null

    private val DEFAULT_ZOOM = 16f

    private var mMarker: Marker? = null

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?,
        session: Session
    ): super() {
        this.rootView = inflater.inflate(layoutId(), parent, false)
        this.session = session

        val sessionDescription = rootView?.findViewById<TextView>(R.id.description)
        sessionDescription?.text = buildDescription()

        val mapFragment = supportFragmentManager?.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val startRecordingButton = rootView?.findViewById<Button>(R.id.start_recording_button)
        startRecordingButton?.setOnClickListener {
            onStartRecordingClicked()
        }
    }

    abstract fun layoutId(): Int

    protected fun updateMarkerPosition(latitude: Double?, longitude: Double?) {
        if (latitude == null || longitude == null) return

        mMarker?.position = LatLng(latitude, longitude)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap ?: return
        val sessionLocation = session!!.location!!
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
}
