package pl.llp.aircasting.screens.dashboard.charts

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.expanded_session_view.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.MeasurementColor
import pl.llp.aircasting.lib.TemperatureConverter
import pl.llp.aircasting.lib.temperatureFromFahrenheitToCelsius
import pl.llp.aircasting.screens.dashboard.SessionPresenter


class Chart(context: Context, rootView: View?) {
    private val mContext: Context = context
    private val mRootView: View? = rootView
    private val mChartStartTimeTextView: TextView? = mRootView?.chart_start_time
    private val mChartEndTimeTextView: TextView? = mRootView?.chart_end_time
    private val mChartUnitTextView: TextView? = mRootView?.chart_unit

    private var mEntries: List<Entry> = listOf()

    private var mLineChart: LineChart? = mRootView?.chart_view
    private var mDataSet: LineDataSet? = null
    private var mSessionPresenter: SessionPresenter? = null

    fun bindChart(
        sessionPresenter: SessionPresenter?
    ) {
        val session = sessionPresenter?.session
        mSessionPresenter = sessionPresenter

        setEntries(sessionPresenter)

        if (session != null && session.streams.isNotEmpty()) {
            resetChart()
            mDataSet = prepareDataSet()
            drawChart()
        }
        setTimesAndUnit()
    }

    private fun resetChart() {
        mLineChart?.data?.clearValues()
        mLineChart?.clear()
    }

    private fun setEntries(sessionPresenter: SessionPresenter?) {
        mEntries = sessionPresenter?.chartData?.getEntries(sessionPresenter.selectedStream)
            ?: listOf()
    }

    private fun drawChart() {
        // Horizontal grid and no Y Axis labels
        val rightYAxis = mLineChart?.axisRight
        rightYAxis?.gridColor = ContextCompat.getColor(mContext, R.color.aircasting_grey_100)
        rightYAxis?.setDrawLabels(false)
        rightYAxis?.setDrawAxisLine(false)

        // Drawing grid even on an empty chart
        rightYAxis?.axisMinimum = 0f
        rightYAxis?.axisMaximum = 100f

        //Removing bottom "border" and Y values
        val leftYAxis = mLineChart?.axisLeft
        leftYAxis?.gridColor = Color.TRANSPARENT
        leftYAxis?.setDrawAxisLine(false)
        leftYAxis?.setDrawLabels(false)

        // No labels on X Axis and no
        val xAxis = mLineChart?.xAxis
        xAxis?.setDrawLabels(false)
        xAxis?.setDrawAxisLine(false)

        // Chart will not stretch even if there are less than 9 entries
        xAxis?.spaceMin = (ChartAveragesCreator.MAX_AVERAGES_AMOUNT - mEntries.size).toFloat()

        // Removing vertical lines
        xAxis?.gridColor = Color.TRANSPARENT

        // Remove borders
        mLineChart?.setDrawBorders(false)
        mLineChart?.setBorderColor(Color.TRANSPARENT)

        // DATASET
        val lineData = LineData(mDataSet)

        // Formatting values on the chart (no decimal places)
        lineData.setValueFormatter(ChartValueFormatter())

        mLineChart?.clear()
        mLineChart?.data = lineData

        // Removing the legend for colors
        mLineChart?.legend?.isEnabled = false

        // Removing description on the down right
        mLineChart?.description?.isEnabled = false

        // Disabling chart scalability by pinching
        mLineChart?.setScaleEnabled(false)

        // Disabling chart touch event
        mLineChart?.setTouchEnabled(false)

        // Refreshing the chart
        mLineChart?.invalidate()
    }

    private fun prepareDataSet(): LineDataSet {
        if (mEntries == null || mEntries.isEmpty()) {
            return LineDataSet(listOf(), "")
        }

        val dataSet: LineDataSet =
            if (mSessionPresenter?.selectedStream?.measurementType == "Temperature" && TemperatureConverter.isCelsiusToggleEnabled()) {
                val celsiusEntries: List<Entry> = mEntries.map { entry ->
                    Entry(entry.x, temperatureFromFahrenheitToCelsius(entry.y))
                }
                LineDataSet(celsiusEntries, "")
            } else LineDataSet(mEntries, "")

        // Making the line a curve, not a polyline
        dataSet.mode = LineDataSet.Mode.LINEAR

        // Circle colors
        dataSet.circleRadius = 3.5f
        dataSet.circleColors = circleColors()
        dataSet.setDrawCircleHole(false)

        // Line color
        dataSet.color = ContextCompat.getColor(mContext, R.color.aircasting_grey_300)
        dataSet.lineWidth = 1f

        // Values size and color
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = ContextCompat.getColor(mContext, R.color.aircasting_grey_700)

        return dataSet
    }

    private fun circleColors(): List<Int> {
        return mEntries.map { entry ->
            getColor(entry.y)
        }
    }

    private fun getColor(value: Float?): Int {
        val measurementValue = value?.toDouble() ?: 0.0
        return MeasurementColor.forMap(
            mContext,
            measurementValue,
            mSessionPresenter?.sensorThresholdFor(mSessionPresenter?.selectedStream)
        )
    }

    private fun setTimesAndUnit() {
        mChartStartTimeTextView?.text = mSessionPresenter?.chartData?.entriesStartTime
        mChartEndTimeTextView?.text = mSessionPresenter?.chartData?.entriesEndTime
        mChartUnitTextView?.text = chartUnitText()
    }

    private fun chartUnitText(): String {
        if (mSessionPresenter?.selectedStream?.measurementType == "Temperature")
            return "${mContext.getString(chartUnitLabelId())} - ${mSessionPresenter?.selectedStream?.detailedType}"

        return "${mContext.getString(chartUnitLabelId())} - ${mSessionPresenter?.selectedStream?.unitSymbol}"
    }

    private fun chartUnitLabelId(): Int {
        return if (mSessionPresenter?.isFixed()!!) {
            R.string.fixed_session_units_label
        } else {
            R.string.mobile_session_units_label
        }
    }
}
