package io.lunarlogic.aircasting.screens.dashboard.following

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session

class FollowingSessionViewMvcImpl: BaseObservableViewMvc<FollowingSessionViewMvc.Listener>,
    FollowingSessionViewMvc {
    private var mDateTextView: TextView
    private var mNameTextView: TextView
    private var mTagsTextView: TextView
    private var mMeasurementsTextView: TextView

    private var mSession: Session? = null

    constructor(inflater: LayoutInflater, parent: ViewGroup) {
        this.rootView = inflater.inflate(R.layout.following_session, parent, false)

        mDateTextView = findViewById(R.id.following_session_date)
        mNameTextView = findViewById(R.id.following_session_name)
        mTagsTextView = findViewById(R.id.following_session_tags)
        mMeasurementsTextView = findViewById(R.id.session_measurements)
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