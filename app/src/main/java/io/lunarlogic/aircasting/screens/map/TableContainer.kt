package io.lunarlogic.aircasting.screens.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.forEach
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.screens.common.SelectedSensorBorder
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session
import kotlinx.android.synthetic.main.activity_map.view.*

class TableContainer {
    private val mContext: Context
    private val mLayoutInflater: LayoutInflater

    private val mSessionMeasurementsTable: TableLayout?
    private val mSessionMeasurementHeaders: TableRow?
    private val mMeasurementValues: TableRow?

    constructor(context: Context, inflater: LayoutInflater, rootView: View?) {
        mContext = context
        mLayoutInflater = inflater

        mSessionMeasurementsTable = rootView?.measurements_table
        mSessionMeasurementHeaders = rootView?.measurement_headers
        mMeasurementValues = rootView?.measurement_values
    }

    fun bindSession(session: Session, selectedStream: MeasurementStream?, onMeasurementStreamChanged: (MeasurementStream) -> Unit) {
        if (session.measurementsCount() > 0) {
            resetMeasurementsView()
            bindMeasurements(session, selectedStream, onMeasurementStreamChanged)
            stretchTableLayout(session)
        }
    }

    private fun resetMeasurementsView() {
        mSessionMeasurementsTable?.isStretchAllColumns = false
        mSessionMeasurementHeaders?.removeAllViews()
    }

    private fun bindMeasurements(session: Session, selectedStream: MeasurementStream?, onMeasurementStreamChanged: (MeasurementStream) -> Unit) {
        session.streamsSortedByDetailedType().forEach { stream ->
            bindStream(stream.detailedType)
            bindLastMeasurement(stream, selectedStream, onMeasurementStreamChanged)
        }
    }

    private fun stretchTableLayout(session: Session) {
        if (session.streams.size > 1) {
            mSessionMeasurementsTable?.isStretchAllColumns = true
        }
    }

    private fun bindStream(detailedType: String?) {
        val headerView = mLayoutInflater.inflate(R.layout.measurement_header, null, false)

        val headerTextView = headerView.findViewById<TextView>(R.id.measurement_header)
        headerTextView.text = detailedType

        mSessionMeasurementHeaders?.addView(headerView)
    }

    private fun bindLastMeasurement(stream: MeasurementStream, selectedStream: MeasurementStream?, onMeasurementStreamChanged: (MeasurementStream) -> Unit) {
        val measurement = stream.measurements.lastOrNull() ?: return

        val valueView = mLayoutInflater.inflate(R.layout.measurement_value, null, false)
        valueView.background = null

        val circleView = valueView.findViewById<ImageView>(R.id.circle_indicator)
        val valueTextView = valueView.findViewById<TextView>(R.id.measurement_value)

        valueTextView.text = measurement.valueString()

        val color = MeasurementColor.forMap(mContext, measurement, stream)
        circleView.setColorFilter(color)

        if (stream == selectedStream) {
            valueView.background = SelectedSensorBorder(color)
        }

        valueView.setOnClickListener {
            mMeasurementValues?.forEach { it.background = null }
            valueView.background = SelectedSensorBorder(color)

            onMeasurementStreamChanged(stream)
        }

        mMeasurementValues?.addView(valueView)
    }
}
