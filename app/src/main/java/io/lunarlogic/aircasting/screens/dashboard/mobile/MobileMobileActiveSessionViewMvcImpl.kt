package io.lunarlogic.aircasting.screens.dashboard.mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session

class MobileMobileActiveSessionViewMvcImpl: BaseObservableViewMvc<MobileActiveSessionViewMvc.Listener>,
    MobileActiveSessionViewMvc {
    private var mDateTextView: TextView
    private var mNameTextView: TextView
    private var mTagsTextView: TextView
    private var mMeasurementsTextView: TextView
    private var mStopSesssionButton: Button

    private var mSession: Session? = null

    constructor(inflater: LayoutInflater, parent: ViewGroup) {
        this.rootView = inflater.inflate(R.layout.active_session, parent, false)

        mDateTextView = findViewById(R.id.session_date)
        mNameTextView = findViewById(R.id.session_name)
        mTagsTextView = findViewById(R.id.session_tags)
        mMeasurementsTextView = findViewById(R.id.session_measurements)
        mStopSesssionButton = findViewById(R.id.stop_session_button)

        mStopSesssionButton.setOnClickListener(View.OnClickListener {
            for (listener in listeners) {
                listener.onSessionStopClicked(mSession!!)
            }
        })
    }

    override fun bindSession(session: Session) {
        mSession = session
        mDateTextView.setText(session.startTime.toString())
        mNameTextView.setText(session.name)
        mTagsTextView.setText(session.tags.joinToString(", "))

        // TODO: handle
        val measurementsString = session.streams.map { stream ->
            val measurement = stream.measurements.lastOrNull()
            "${stream.detailedType}: ${measurementString(measurement, stream)}"
        }.joinToString("\n")
        mMeasurementsTextView.setText(measurementsString)
    }

    private fun measurementString(measurement: Measurement?, stream: MeasurementStream): String {
        if (measurement == null) {
            return ""
        }

        return "%.2f %s".format(measurement.value, stream.unitSymbol)
    }
}