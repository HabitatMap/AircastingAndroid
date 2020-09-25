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
    private val mRootView: View?
    private val mLayoutInflater: LayoutInflater

    private var mSelectable: Boolean
    private var mDisplayValues: Boolean

    private val mMeasurementStreams: MutableList<MeasurementStream> = mutableListOf()
    private val mLastMeasurementColors: HashMap<MeasurementStream, Int> = HashMap()

    private val mMeasurementsTable: TableLayout?
    private val mMeasurementHeaders: TableRow?
    private var mMeasurementValues: TableRow? = null

    private val mHeaderColor: Int
    private val mSelectedHeaderColor: Int

    private var mSession: Session? = null
    private var mSelectedStream: MeasurementStream? = null
    private var mOnMeasurementStreamChanged: ((MeasurementStream) -> Unit)? = null

    constructor(context: Context, inflater: LayoutInflater, rootView: View?, selectable: Boolean = false, displayValues: Boolean = false) {
        mContext = context
        mRootView = rootView
        mLayoutInflater = inflater

        mSelectable = selectable
        mDisplayValues = displayValues

        mMeasurementsTable = rootView?.measurements_table
        mMeasurementHeaders = rootView?.measurement_headers

        if (mDisplayValues) {
            mMeasurementValues = rootView?.measurement_values
        }

        mHeaderColor = ResourcesCompat.getColor(mContext.resources, R.color.aircasting_grey_400, null)
        mSelectedHeaderColor = ResourcesCompat.getColor(mContext.resources, R.color.aircasting_dark_blue, null)
    }

    fun makeSelectable() {
        mSelectable = true
        mDisplayValues = true
        mMeasurementValues = mRootView?.measurement_values

        refresh()
    }

    fun makeStatic(displayValues: Boolean = true) {
        resetMeasurementsView()

        mSelectable = false
        mDisplayValues = displayValues
        if (!displayValues) mMeasurementValues = null

        refresh()
    }

    fun refresh() {
        bindSession(mSession, mSelectedStream, mOnMeasurementStreamChanged)
    }

    fun bindSession(
        session: Session?,
        selectedStream: MeasurementStream? = null,
        onMeasurementStreamChanged: ((MeasurementStream) -> Unit)? = null
    ) {
        mSession = session
        mSelectedStream = selectedStream
        mOnMeasurementStreamChanged = onMeasurementStreamChanged

        if (session != null && session.measurementsCount() > 0) {
            resetMeasurementsView()
            bindMeasurements(session, selectedStream, onMeasurementStreamChanged)
            stretchTableLayout(session)
        }
    }

    private fun resetMeasurementsView() {
        mMeasurementsTable?.isStretchAllColumns = false
        mMeasurementHeaders?.removeAllViews()
        mMeasurementValues?.removeAllViews()
    }

    private fun bindMeasurements(
        session: Session,
        selectedStream: MeasurementStream?,
        onMeasurementStreamChanged: ((MeasurementStream) -> Unit)? = null
    ) {
        session.streamsSortedByDetailedType().forEach { stream ->
            bindStream(stream, selectedStream, onMeasurementStreamChanged)
            bindLastMeasurement(stream, selectedStream, onMeasurementStreamChanged)
        }
    }

    private fun stretchTableLayout(session: Session) {
        if (session.streams.size > 1) {
            mMeasurementsTable?.isStretchAllColumns = true
        }
    }

    private fun bindStream(
        stream: MeasurementStream,
        selectedStream: MeasurementStream?,
        onMeasurementStreamChanged: ((MeasurementStream) -> Unit)? = null
    ) {
        val headerView = mLayoutInflater.inflate(R.layout.measurement_header, null, false)

        val headerTextView = headerView.findViewById<TextView>(R.id.measurement_header)
        headerTextView.text = stream.detailedType

        mMeasurementHeaders?.addView(headerView)
        mMeasurementStreams.add(stream)

        if (mSelectable) {
            if (stream == selectedStream) {
                markMeasurementHeaderAsSelected(headerTextView)
            }

            headerView.setOnClickListener {
                mSelectedStream = stream
                resetSensorSelection()

                markMeasurementHeaderAsSelected(stream)
                markMeasurementValueAsSelected(stream)

                onMeasurementStreamChanged?.invoke(stream)
            }
        }
    }

    private fun resetSensorSelection() {
        mMeasurementHeaders?.forEach { resetMeasurementHeader(it) }
        mMeasurementValues?.forEach { it.background = null }
    }

    private fun resetMeasurementHeader(headerView: View) {
        val headerTextView = headerView.findViewById<TextView>(R.id.measurement_header)
        headerTextView.setTypeface(null, Typeface.NORMAL)
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
        headerTextView.setTypeface(null, Typeface.BOLD)
        headerTextView.setTextColor(mSelectedHeaderColor)
    }

    private fun markMeasurementValueAsSelected(stream: MeasurementStream) {
        val index = mMeasurementStreams.indexOf(stream)
        val color = mLastMeasurementColors[stream]

        try {
            val valueView = mMeasurementValues?.get(index)
            if (valueView != null && color != null) {
                valueView.background = SelectedSensorBorder(color)
            }
        } catch(e: IndexOutOfBoundsException) {}
    }

    private fun bindLastMeasurement(
        stream: MeasurementStream,
        selectedStream: MeasurementStream?,
        onMeasurementStreamChanged: ((MeasurementStream) -> Unit)? = null
    ) {
        val measurement = stream.measurements.lastOrNull() ?: return

        val valueView = mLayoutInflater.inflate(R.layout.measurement_value, null, false)
        valueView.background = null

        val circleView = valueView.findViewById<ImageView>(R.id.circle_indicator)
        val valueTextView = valueView.findViewById<TextView>(R.id.measurement_value)

        valueTextView.text = measurement.valueString()

        val color = MeasurementColor.forMap(mContext, measurement, stream)
        circleView.setColorFilter(color)
        mLastMeasurementColors[stream] = color
        
        mMeasurementValues?.addView(valueView)

        if (mSelectable) {
            if (stream == selectedStream) {
                valueView.background = SelectedSensorBorder(color)
            }

            valueView.setOnClickListener {
                resetSensorSelection()

                mSelectedStream = stream
                markMeasurementHeaderAsSelected(stream)
                valueView.background = SelectedSensorBorder(color)

                onMeasurementStreamChanged?.invoke(stream)
            }
        }
    }
}
