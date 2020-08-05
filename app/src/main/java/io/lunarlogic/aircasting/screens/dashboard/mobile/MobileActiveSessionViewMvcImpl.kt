package io.lunarlogic.aircasting.screens.dashboard.mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session

class MobileActiveSessionViewMvcImpl: BaseObservableViewMvc<MobileActiveSessionViewMvc.Listener>,
    MobileActiveSessionViewMvc {
    private val mLayoutInflater: LayoutInflater

    private var mDateTextView: TextView
    private var mNameTextView: TextView
    private var mTagsTextView: TextView
    private var mMeasurementsTable: TableLayout
    private var mMeasurementHeaders: TableRow
    private var mMeasurementValues: TableRow
//    private var mStopSesssionButton: Button

    private var mSession: Session? = null

    constructor(inflater: LayoutInflater, parent: ViewGroup) {
        mLayoutInflater = inflater

        this.rootView = inflater.inflate(R.layout.active_session, parent, false)

        mDateTextView = findViewById(R.id.active_session_date)
        mNameTextView = findViewById(R.id.active_session_name)
        mTagsTextView = findViewById(R.id.active_session_tags)
        mMeasurementsTable = findViewById(R.id.measurements_table)
        mMeasurementHeaders = findViewById(R.id.measurement_headers)
        mMeasurementValues = findViewById(R.id.measurement_values)

//        mStopSesssionButton = findViewById(R.id.stop_session_button)
//
//        mStopSesssionButton.setOnClickListener(View.OnClickListener {
//            for (listener in listeners) {
//                listener.onSessionStopClicked(mSession!!)
//            }
//        })
    }

    override fun bindSession(session: Session) {
        mSession = session
        mDateTextView.setText(session.durationString())
        mNameTextView.setText(session.name)
        mTagsTextView.setText(session.tags.joinToString(", "))

        mMeasurementsTable.isStretchAllColumns = false
        mMeasurementHeaders.removeAllViews()
        mMeasurementValues.removeAllViews()
        session.streams.sortedBy { it.detailedType }.forEach { stream ->
            bindStream(stream.detailedType)
            bindLastMeasurement(stream)
        }

        if (session.streams.size > 1) {
            mMeasurementsTable.isStretchAllColumns = true
        }
    }

    private fun bindStream(detailedType: String?) {
        val headerView = mLayoutInflater.inflate(R.layout.measurement_header, null, false)

        val headerTextView = headerView.findViewById<TextView>(R.id.measurement_header)
        headerTextView.text = detailedType

        mMeasurementHeaders.addView(headerView)
    }

    private fun bindLastMeasurement(stream: MeasurementStream) {
        val measurement = stream.measurements.lastOrNull()
        val valueView = mLayoutInflater.inflate(R.layout.measurement_value, null, false)

        val circleView = valueView.findViewById<ImageView>(R.id.circle_indicator)
        val valueTextView = valueView.findViewById<TextView>(R.id.measurement_value)

        if (measurement == null) {
            circleView.visibility = View.GONE
        } else {
            valueTextView.text = measurementValueString(measurement, stream)
        }

        mMeasurementValues.addView(valueView)
    }

    private fun measurementValueString(measurement: Measurement, stream: MeasurementStream): String {
        return "%.0f".format(measurement.value)
    }
}
