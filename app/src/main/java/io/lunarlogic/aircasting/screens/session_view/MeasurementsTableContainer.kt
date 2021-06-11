package io.lunarlogic.aircasting.screens.session_view

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.view.forEach
import androidx.core.view.get
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import kotlinx.android.synthetic.main.session_card.view.*


class MeasurementsTableContainer {
    private val mContext: Context
    private val mRootView: View?
    private val mLayoutInflater: LayoutInflater

    private var mSelectable: Boolean
    private var mDisplayValues: Boolean
    private var mDisplayAvarages = false
    private var mCollapsed: Boolean = false

    private val mMeasurementStreams: MutableList<MeasurementStream> = mutableListOf()
    private val mLastMeasurementColors: HashMap<String, Int> = HashMap()

    private val mMeasurementsTable: TableLayout?
    private val mMeasurementHeaders: TableRow?
    private var mMeasurementValues: TableRow? = null

    private var mSessionPresenter: SessionPresenter? = null
    private var mOnMeasurementStreamChanged: ((MeasurementStream) -> Unit)? = null
    private var expandCard: (() -> Unit?)? = null
    private var onExpandSessionCard: (() -> Unit?)? = null

    constructor(
        context: Context,
        inflater: LayoutInflater,
        rootView: View?,
        selectable: Boolean = false,
        displayValues: Boolean = false
    ) {
        mContext = context
        mRootView = rootView
        mLayoutInflater = inflater

        mSelectable = selectable
        mDisplayValues = displayValues

        mCollapsed = displayValues

        mMeasurementsTable = rootView?.measurements_table
        mMeasurementHeaders = rootView?.measurement_headers

        if (mDisplayValues) mMeasurementValues = rootView?.measurement_values

    }

    fun makeSelectable(displayValues: Boolean = true) {
        mSelectable = true
        mDisplayValues = displayValues
        if (displayValues) mMeasurementValues = mRootView?.measurement_values

        refresh()
    }

    fun makeCollapsed(displayValues: Boolean = true) {
        resetMeasurementsView()
        mSelectable = true
        mCollapsed = true

        mDisplayValues = displayValues
         if (!displayValues && mCollapsed) mMeasurementValues = null
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
        mDisplayAvarages = mSessionPresenter?.isMobileDormant() ?: false

        val session = mSessionPresenter?.session
        if (session != null && session.activeStreams.count() > 0) {
            resetMeasurementsView()
            bindMeasurements()
            stretchTableLayout()
        }
    }

    fun bindExpandCardCallbacks(expandCardCallback: (() -> Unit?)?, onExpandSessionCardClickedCallback: (() -> Unit?)?) {
        expandCard = expandCardCallback
        onExpandSessionCard = onExpandSessionCardClickedCallback
        mCollapsed = false
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
            bindMeasurement(stream)
        }
    }

    private fun stretchTableLayout() {
        val session = mSessionPresenter?.session
        if (session != null && session.activeStreams.size > 1) {
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
                changeSelectedStream(stream)
            }
        }
    }

    private fun onMeasurementClicked(stream: MeasurementStream) {
        resetSensorSelection()
        mOnMeasurementStreamChanged?.invoke(stream)
    }

    private fun resetSensorSelection() {
        mMeasurementHeaders?.forEach { resetMeasurementHeader(it) }
        mMeasurementValues?.forEach { resetValueViewBorder(it as LinearLayout) }
    }

    private fun resetMeasurementHeader(headerView: View) {
        val headerTextView = headerView.findViewById<TextView>(R.id.measurement_header)
        headerTextView.setTextAppearance(mContext, R.style.TextAppearance_Aircasting_MeasurementsTableHeader)
    }

    private fun markMeasurementHeaderAsSelected(stream: MeasurementStream) {
        onExpandSessionCard?.invoke()
        expandCard?.invoke()
        val index = mMeasurementStreams.indexOf(stream)
        try {
            val headerView = mMeasurementHeaders?.get(index)
            val headerTextView = headerView?.findViewById<TextView>(R.id.measurement_header)
            headerTextView?.let { markMeasurementHeaderAsSelected(headerTextView) }
        } catch(e: IndexOutOfBoundsException) {}
    }

    private fun markMeasurementHeaderAsSelected(headerTextView: TextView) {
        headerTextView.setTextAppearance(mContext, R.style.TextAppearance_Aircasting_MeasurementsTableHeaderSelected)
    }

    private fun markMeasurementValueAsSelected(stream: MeasurementStream) {
        val index = mMeasurementStreams.indexOf(stream)
        val color = mLastMeasurementColors[stream.sensorName]

        try {
            val valueViewContainer: LinearLayout = mMeasurementValues?.get(index) as LinearLayout
            if (valueViewContainer != null && color != null) {
                setValueViewBorder(valueViewContainer, color)
            }
        } catch(e: IndexOutOfBoundsException) {}
    }

    private fun bindMeasurement(stream: MeasurementStream) {
        val measurementValue = getMeasurementValue(stream) ?: return

        val color = MeasurementColor.forMap(mContext, measurementValue, mSessionPresenter?.sensorThresholdFor(stream))
        mLastMeasurementColors[stream.sensorName] = color

        val valueViewContainer = renderValueView(measurementValue, color, stream)
        mMeasurementValues?.addView(valueViewContainer)
        
        if (mSelectable) {
            if (stream == mSessionPresenter?.selectedStream) {
                setValueViewBorder(valueViewContainer, color)
            }

            valueViewContainer.setOnClickListener {
                onMeasurementClicked(stream)

                markMeasurementHeaderAsSelected(stream)
                setValueViewBorder(valueViewContainer, color)
            }
        }
    }

    private fun getMeasurementValue(stream: MeasurementStream): Double? {
        return if (mDisplayAvarages) {
            stream.getAvgMeasurement()
        } else {
            stream.measurements.lastOrNull()?.value
        }
    }

    private fun shouldDisplayDisconnectedIndicator(): Boolean {
        return mSessionPresenter?.isFixed() == false && mSessionPresenter?.isDisconnected() == true
    }

    private fun renderValueView(measurementValue: Double, color: Int, stream: MeasurementStream): LinearLayout {
        val valueView = mLayoutInflater.inflate(R.layout.measurement_value, null, false)

        val circleView = valueView.findViewById<ImageView>(R.id.circle_indicator)
        val valueTextView = valueView.findViewById<TextView>(R.id.measurement_value)

        if (shouldDisplayDisconnectedIndicator()) {
            valueTextView.text = "-"
            circleView.visibility = View.GONE
        } else {
            valueTextView.text = Measurement.formatValue(measurementValue)
            circleView.visibility = View.VISIBLE
            circleView.setColorFilter(color)
        }

        valueView.background = null
        valueTextView.setOnClickListener {
            changeSelectedStream(stream)
        }

        val containerLayout = LinearLayout(mContext)
        containerLayout.gravity = Gravity.CENTER
        containerLayout.addView(valueView)

        return containerLayout
    }

    private fun setValueViewBorder(valueViewContainer: LinearLayout, color: Int) {
        val valueView = valueViewContainer.getChildAt(0)
        valueView.background = SelectedSensorBorder(color)
    }

    private fun resetValueViewBorder(valueViewContainer: LinearLayout) {
        val valueView = valueViewContainer.getChildAt(0)
        valueView.background = null
    }

    private fun changeSelectedStream(stream: MeasurementStream) {
        onMeasurementClicked(stream)

        markMeasurementHeaderAsSelected(stream)
        markMeasurementValueAsSelected(stream)
    }
}
