package io.lunarlogic.aircasting.screens.common

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
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.sensor.MeasurementStream
//import kotlinx.android.synthetic.main.activity_map.view.*
import kotlinx.android.synthetic.main.session_card.view.*


class MeasurementsTableContainer {
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

    private var mSessionPresenter: SessionPresenter? = null
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
        bindSession(mSessionPresenter, mOnMeasurementStreamChanged)
    }

    fun bindSession(
        sessionPresenter: SessionPresenter?,
        onMeasurementStreamChanged: ((MeasurementStream) -> Unit)? = null
    ) {
        mSessionPresenter = sessionPresenter
        mOnMeasurementStreamChanged = onMeasurementStreamChanged

        val session = mSessionPresenter?.session
        if (session != null && session.streams.count() > 0) {
            resetMeasurementsView()
            bindMeasurements()
            stretchTableLayout()
        }
    }

    private fun resetMeasurementsView() {
        mMeasurementsTable?.isStretchAllColumns = false
        mMeasurementHeaders?.removeAllViews()
        mMeasurementValues?.removeAllViews()
        mMeasurementStreams.clear()
    }

    private fun bindMeasurements() {
        val session = mSessionPresenter?.session
        session?.streamsSortedByDetailedType()?.forEach { stream ->
            bindStream(stream)
            bindLastMeasurement(stream)
        }
    }

    private fun stretchTableLayout() {
        val session = mSessionPresenter?.session
        if (session != null && session.streams.size > 1) {
            mMeasurementsTable?.isStretchAllColumns = true
        }
    }

    private fun bindStream(stream: MeasurementStream) {
        val headerView = mLayoutInflater.inflate(R.layout.measurement_header, null, false)

        val headerTextView = headerView.findViewById<TextView>(R.id.measurement_header)
        headerTextView.text = stream.detailedType

        mMeasurementHeaders?.addView(headerView)
        mMeasurementStreams.add(stream)

        if (mSelectable) {
            if (stream == mSessionPresenter?.selectedStream) {
                markMeasurementHeaderAsSelected(headerTextView)
            }

            headerView.setOnClickListener {
                onMeasurementClicked(stream)

                markMeasurementHeaderAsSelected(stream)
                markMeasurementValueAsSelected(stream)
            }
        }
    }

    private fun onMeasurementClicked(stream: MeasurementStream) {
        resetSensorSelection()
        mOnMeasurementStreamChanged?.invoke(stream)
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

    private fun bindLastMeasurement(stream: MeasurementStream) {
        val measurement = stream.measurements.lastOrNull() ?: return

        val valueView = mLayoutInflater.inflate(R.layout.measurement_value, null, false)
        valueView.background = null

        val circleView = valueView.findViewById<ImageView>(R.id.circle_indicator)
        val valueTextView = valueView.findViewById<TextView>(R.id.measurement_value)

        valueTextView.text = measurement.valueString()

        val color = MeasurementColor.forMap(mContext, measurement, mSessionPresenter?.sensorThresholdFor(stream))
        circleView.setColorFilter(color)
        mLastMeasurementColors[stream] = color

        mMeasurementValues?.addView(valueView)
        
        if (mSelectable) {
            if (stream == mSessionPresenter?.selectedStream) {
                valueView.background = SelectedSensorBorder(color)
            }

            valueView.setOnClickListener {
                onMeasurementClicked(stream)

                markMeasurementHeaderAsSelected(stream)
                valueView.background = SelectedSensorBorder(color)
            }
        }
    }
}
