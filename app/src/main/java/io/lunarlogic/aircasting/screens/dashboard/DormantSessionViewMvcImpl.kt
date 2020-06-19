package io.lunarlogic.aircasting.screens.dashboard

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

class DormantSessionViewMvcImpl: BaseObservableViewMvc<DormantSessionViewMvc.Listener>,
    DormantSessionViewMvc {
    private var mDateTextView: TextView
    private var mNameTextView: TextView
    private var mTagsTextView: TextView
    private var mMeasurementsTextView: TextView
    private var mDeleteSesssionButton: Button

    private var mSession: Session? = null

    constructor(inflater: LayoutInflater, parent: ViewGroup) {
        this.rootView = inflater.inflate(R.layout.dormant_session, parent, false)

        mDateTextView = findViewById(R.id.session_date)
        mNameTextView = findViewById(R.id.session_name)
        mTagsTextView = findViewById(R.id.session_tags)
        mMeasurementsTextView = findViewById(R.id.session_measurements)
        mDeleteSesssionButton = findViewById(R.id.delete_session_button)

        mDeleteSesssionButton.setOnClickListener(View.OnClickListener {
            for (listener in listeners) {
                listener.onSessionDeleteClicked(mSession!!)
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

        return "${measurement.value} ${stream.unitSymbol}"
    }
}