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
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.sensor.Session
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

abstract class ConfirmationViewMvcImpl: BaseObservableViewMvc<ConfirmationViewMvc.Listener>, ConfirmationViewMvc,
    OnMapReadyCallback {
    private var session: Session? = null

    private val DEFAULT_ZOOM = 13f

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

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap ?: return
        val sessionLocation = session!!.location!!
        val location = LatLng(sessionLocation.latitude, sessionLocation.longitude)
        googleMap.addMarker(
            MarkerOptions()
                .position(location))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM))
    }

    private fun onStartRecordingClicked() {
        for (listener in listeners) {
            listener.onStartRecordingClicked(session!!)
        }
    }
}