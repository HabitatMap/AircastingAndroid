package pl.llp.aircasting.ui.view.screens.session_view.measurement_table_container

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.view.forEach
import androidx.core.view.get
import kotlinx.android.synthetic.main.session_card.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.session_view.SelectedSensorBorder
import pl.llp.aircasting.util.MeasurementColor
import pl.llp.aircasting.util.TemperatureConverter
import pl.llp.aircasting.util.extensions.gone
import pl.llp.aircasting.util.extensions.setAppearance
import pl.llp.aircasting.util.extensions.visible

abstract class MeasurementsTableContainer {
    private val mContext: Context
    private val mRootView: View?
    private val mLayoutInflater: LayoutInflater

    private var mSelectable: Boolean
    private var mDisplayValues: Boolean
    private var mDisplayAvarages = false
    protected var mCollapsed: Boolean = false

    private val mMeasurementStreams: MutableList<MeasurementStream> = mutableListOf()
    private val mLastMeasurementColors: HashMap<String, Int> = HashMap()

    private val mMeasurementsTable: TableLayout?
    private val mMeasurementHeaders: TableRow?
    private var mMeasurementValues: TableRow? = null

    protected var mSessionPresenter: SessionPresenter? = null
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

    abstract fun shouldShowSelectedMeasurement(stream: MeasurementStream): Boolean

    fun makeSelectable(displayValues: Boolean = true) {
        mSelectable = true
        mDisplayValues = displayValues
        if (displayValues) mMeasurementValues = mRootView?.measurement_values
    }

    fun makeCollapsed(sessionPresenter: SessionPresenter?, displayValues: Boolean = true) {
        resetMeasurementsView()
        mSelectable = true
        mCollapsed = true

        mDisplayValues = displayValues
        if (!displayValues && mCollapsed) mMeasurementValues = null
        refresh(sessionPresenter)
    }

    fun refresh(sessionPresenter: SessionPresenter?) {
        bindSession(sessionPresenter, mOnMeasurementStreamChanged)
    }

    fun bindSession(
        sessionPresenter: SessionPresenter?,
        onMeasurementStreamChanged: ((MeasurementStream) -> Unit)? = null
    ) {
        mSessionPresenter = sessionPresenter
        mOnMeasurementStreamChanged = onMeasurementStreamChanged
        mDisplayAvarages = mSessionPresenter?.isMobileDormant() ?: false

        resetMeasurementsView()
        bindMeasurements()
        stretchTableLayout()

        if (mSessionPresenter?.session?.status == Session.Status.DISCONNECTED &&
            mSessionPresenter?.session?.type == Session.Type.MOBILE
        ) {
            mMeasurementsTable?.gone()
        } else {
            mMeasurementsTable?.visible()
        }
    }

    fun bindExpandCardCallbacks(
        expandCardCallback: (() -> Unit?)?,
        onExpandSessionCardClickedCallback: (() -> Unit?)?
    ) {
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
            if (stream.isMeasurementTypeTemperature())
                TemperatureConverter.setAppropriateDetailedType(stream)

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
            if (shouldShowSelectedMeasurement(stream)) {
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
        headerTextView.setAppearance(
            mContext,
            R.style.TextAppearance_Aircasting_MeasurementsTableHeader
        )
    }

    private fun markMeasurementHeaderAsSelected(stream: MeasurementStream) {
        onExpandSessionCard?.invoke()
        expandCard?.invoke()
        val index = mMeasurementStreams.indexOf(stream)
        try {
            val headerView = mMeasurementHeaders?.get(index)
            val headerTextView = headerView?.findViewById<TextView>(R.id.measurement_header)
            headerTextView?.let { markMeasurementHeaderAsSelected(headerTextView) }
        } catch (e: IndexOutOfBoundsException) {
        }
    }

    private fun markMeasurementHeaderAsSelected(headerTextView: TextView) {
        headerTextView.setAppearance(
            mContext,
            R.style.TextAppearance_Aircasting_MeasurementsTableHeaderSelected
        )
    }

    private fun markMeasurementValueAsSelected(stream: MeasurementStream) {
        val index = mMeasurementStreams.indexOf(stream)
        val color = mLastMeasurementColors[stream.sensorName]

        try {
            val valueViewContainer: LinearLayout = mMeasurementValues?.get(index) as LinearLayout
            if (valueViewContainer != null && color != null) {
                setValueViewBorder(valueViewContainer, color)
            }
        } catch (e: IndexOutOfBoundsException) {
        }
    }

    private fun bindMeasurement(stream: MeasurementStream) {
        var measurementValue = getMeasurementValue(stream) ?: return

        val color = MeasurementColor.forMap(
            mContext,
            measurementValue,
            mSessionPresenter?.sensorThresholdFor(stream)
        )
        mLastMeasurementColors[stream.sensorName] = color

        if (stream.isMeasurementTypeTemperature() && TemperatureConverter.isCelsiusToggleEnabled()) measurementValue =
            TemperatureConverter.fahrenheitToCelsius(measurementValue)

        val valueViewContainer = renderValueView(measurementValue, color, stream)
        mMeasurementValues?.addView(valueViewContainer)

        if (mSelectable) {
            if (stream == mSessionPresenter?.selectedStream) {
                setValueViewBorder(valueViewContainer, color)
            }
        }

    }

    private fun getMeasurementValue(stream: MeasurementStream): Double? {
        return if (mDisplayAvarages) {
            stream.getAvgMeasurement()
        } else {
            stream.getLastMeasurementValue()
        }
    }

    private fun shouldDisplayDisconnectedIndicator(): Boolean {
        return mSessionPresenter?.isFixed() == false && mSessionPresenter?.isDisconnected() == true
    }

    private fun renderValueView(
        measurementValue: Double,
        color: Int,
        stream: MeasurementStream
    ): LinearLayout {
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
        val inflater: LayoutInflater = LayoutInflater.from(mContext)
        val containerLayout: LinearLayout =
            inflater.inflate(R.layout.measurement_table_container_layout, null) as LinearLayout
        containerLayout.apply {
            setOnClickListener { changeSelectedStream(stream) }
            addView(valueView)
        }

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
