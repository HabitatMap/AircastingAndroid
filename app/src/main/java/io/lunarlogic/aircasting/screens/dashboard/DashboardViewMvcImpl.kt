package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.Measurement
import java.util.*

class DashboardViewMvcImpl : BaseObservableViewMvc<DashboardViewMvc.Listener>, DashboardViewMvc {
    private var mRecordSessionButton: Button? = null
    private var mStopSessionButton: Button? = null

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.fragment_dashboard, parent, false)

        mRecordSessionButton = rootView?.findViewById(R.id.dashboard_record_new_session_button)
        mRecordSessionButton?.setOnClickListener {
            onRecordNewSessionClicked()
        }
        mStopSessionButton = rootView?.findViewById(R.id.dashboard_stop_session_button)
        mStopSessionButton?.setOnClickListener {
            onStopSessionClicked()
        }
    }

    override fun updateMeasurements(measurementEvent: NewMeasurementEvent) {
        val textView = rootView?.findViewById<TextView>(R.id.text_dashboard)

        if (measurementEvent.sensorName == "AirBeam2-PM2.5") {
            textView?.text = "Current PM 2.5: " + measurementEvent.measuredValue
        }
    }

    private fun onRecordNewSessionClicked() {
        showStopButton()

        for (listener in listeners) {
            listener.onRecordNewSessionClicked()
        }
    }

    private fun onStopSessionClicked() {
        showRecordNewSessionButton()

        for (listener in listeners) {
            // TODO: change after interface change
            listener.onStopSessionClicked(UUID.randomUUID())
        }
    }

    private fun showStopButton() {
        mRecordSessionButton?.visibility = View.INVISIBLE
        mStopSessionButton?.visibility = View.VISIBLE
    }

    private fun showRecordNewSessionButton() {
        mRecordSessionButton?.visibility = View.VISIBLE
        mStopSessionButton?.visibility = View.INVISIBLE
    }
}