package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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
        inflater: LayoutInflater, parent: ViewGroup?,
        supportFragmentManager: FragmentManager?,
        session: Session
    ): super() {
        this.rootView = inflater.inflate(layoutId(), parent, false)
        this.session = session

        val sessionDescription = rootView?.findViewById<TextView>(R.id.description)
        val sessionDescriptionTemplate = inflater.context.getString(descriptionStringId())
        sessionDescription?.text = sessionDescriptionTemplate.format(session.name)

        val mapFragment = supportFragmentManager?.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val startRecordingButton = rootView?.findViewById<Button>(R.id.start_recording_button)
        startRecordingButton?.setOnClickListener {
            onStartRecordingClicked()
        }
    }

    abstract fun layoutId(): Int
    abstract fun descriptionStringId(): Int

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
}
