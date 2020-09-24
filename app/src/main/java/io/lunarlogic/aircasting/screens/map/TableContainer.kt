package io.lunarlogic.aircasting.screens.map

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.forEach
import androidx.core.view.get
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.screens.common.SelectedSensorBorder
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session
import kotlinx.android.synthetic.main.activity_map.view.*


class TableContainer {
    private val mContext: Context
    private val mLayoutInflater: LayoutInflater

    private val mMeasurementStreams: MutableList<MeasurementStream> = mutableListOf()

    private val mMeasurementsTable: TableLayout?
    private val mMeasurementHeaders: TableRow?
    private val mMeasurementValues: TableRow?

    private val mHeaderColor: Int
    private val mHeaderFont: Typeface?
    private val mSelectedHeaderColor: Int
    private val mSelectedHeaderFont: Typeface?


    constructor(context: Context, inflater: LayoutInflater, rootView: View?) {
        mContext = context
        mLayoutInflater = inflater

        mMeasurementsTable = rootView?.measurements_table
        mMeasurementHeaders = rootView?.measurement_headers
        mMeasurementValues = rootView?.measurement_values

        mHeaderColor = ResourcesCompat.getColor(mContext.resources, R.color.aircasting_grey_400, null)
        mHeaderFont = ResourcesCompat.getFont(mContext, R.font.muli_regular)
        mSelectedHeaderFont = ResourcesCompat.getFont(mContext, R.font.muli_bold)
        mSelectedHeaderColor = ResourcesCompat.getColor(mContext.resources, R.color.aircasting_dark_blue, null)
    }

    fun bindSession(
        session: Session,
        selectedStream: MeasurementStream?,
        onMeasurementStreamChanged: (MeasurementStream) -> Unit
    ) {
        if (session.measurementsCount() > 0) {
            resetMeasurementsView()
            bindMeasurements(session, selectedStream, onMeasurementStreamChanged)
            stretchTableLayout(session)
        }
    }

    private fun resetMeasurementsView() {
        mMeasurementsTable?.isStretchAllColumns = false
        mMeasurementHeaders?.removeAllViews()
    }

    private fun bindMeasurements(
        session: Session,
        selectedStream: MeasurementStream?,
        onMeasurementStreamChanged: (MeasurementStream) -> Unit
    ) {
        session.streamsSortedByDetailedType().forEach { stream ->
            bindStream(stream, selectedStream)
            bindLastMeasurement(stream, selectedStream, onMeasurementStreamChanged)
        }
    }

    private fun stretchTableLayout(session: Session) {
        if (session.streams.size > 1) {
            mMeasurementsTable?.isStretchAllColumns = true
        }
    }

    private fun bindStream(stream: MeasurementStream, selectedStream: MeasurementStream?) {
        val headerView = mLayoutInflater.inflate(R.layout.measurement_header, null, false)

        val headerTextView = headerView.findViewById<TextView>(R.id.measurement_header)
        headerTextView.text = stream.detailedType

        if (stream == selectedStream) {
            markMeasurementHeaderAsSelected(headerTextView)
        }

        mMeasurementHeaders?.addView(headerView)
        mMeasurementStreams.add(stream)
    }

    private fun resetMeasurementHeader(headerView: View) {
        val headerTextView = headerView.findViewById<TextView>(R.id.measurement_header)
        headerTextView.typeface = mHeaderFont
        headerTextView.setTextColor(mHeaderColor)
    }

    private fun markMeasurementHeaderAsSelected(stream: MeasurementStream) {
        val index = mMeasurementStreams.indexOf(stream)
        try {
            val headerView = mMeasurementHeaders?.get(index)
            val headerTextView = headerView?.findViewById<TextView>(R.id.measurement_header)
            headerTextView?.let { markMeasurementHeaderAsSelected(headerTextView) }
        } catch(e: IndexOutOfBoundsException) {}
    }

    private fun markMeasurementHeaderAsSelected(headerTextView: TextView) {
        headerTextView.typeface = mSelectedHeaderFont
        headerTextView.setTextColor(mSelectedHeaderColor)
    }

    private fun bindLastMeasurement(
        stream: MeasurementStream,
        selectedStream: MeasurementStream?,
        onMeasurementStreamChanged: (MeasurementStream) -> Unit
    ) {
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
            mMeasurementHeaders?.forEach { resetMeasurementHeader(it) }
            mMeasurementValues?.forEach { it.background = null }

            markMeasurementHeaderAsSelected(stream)
            valueView.background = SelectedSensorBorder(color)

            onMeasurementStreamChanged(stream)
        }

        mMeasurementValues?.addView(valueView)
    }
}
