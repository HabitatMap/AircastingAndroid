package io.lunarlogic.aircasting.screens.dashboard.charts

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import java.text.DecimalFormat
import kotlinx.android.synthetic.main.session_card.view.*


class Chart {
    private val mContext: Context
    private val mRootView: View?

    private var mEntries: List<Entry> = listOf()

    private var mLineChart: LineChart?
    private var mDataSet: LineDataSet? = null
    private var mSessionPresenter: SessionPresenter? = null

    constructor(
        context: Context,
        rootView: View?

    ) {
        mContext = context
        mRootView = rootView
        mLineChart = mRootView?.chart_view
    }

    fun bindChart(
        sessionPresenter: SessionPresenter?
    ) {
        val session = sessionPresenter?.session
        mSessionPresenter = sessionPresenter
        mEntries = sessionPresenter?.chartData?.getEntries(sessionPresenter.selectedStream) ?: listOf()

        resetChart()
        if (session != null && session?.streams.count() > 0) {
            mDataSet = prepareDataSet()
            drawChart()
        }
    }

    private fun resetChart() {
        mLineChart?.data?.clearValues()
        mLineChart?.clear()
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
        mLineChart?.legend?.isEnabled  = false

        // Removing description on the down right
        mLineChart?.description?.isEnabled = false

        // Disabling chart scalability by pinching
        mLineChart?.setScaleEnabled(false)

        // Refreshing the chart
        mLineChart?.invalidate()
    }

    private fun prepareDataSet(): LineDataSet? {
        if (mEntries == null || mEntries.isEmpty()) {
            return LineDataSet(listOf(), "")
        }
        val dataSet = LineDataSet(mEntries, "")

        // Making the line a curve, not a polyline
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER

        // Circle colors
        dataSet.circleRadius = 3.5f
        dataSet.setCircleColors(circleColors())
        dataSet.setDrawCircleHole(false)

        // Line color
        dataSet.setColor(ContextCompat.getColor(mContext, R.color.aircasting_grey_300))
        dataSet.lineWidth = 1f

        return dataSet
    }

    private fun circleColors(): List<Int>? {
        return mEntries?.map { entry ->
            getColor(entry?.y)
        }
    }

    private fun getColor(value: Float?): Int {
        val measurementValue = value?.toDouble() ?: 0.0
        return  MeasurementColor.forMap(mContext, measurementValue, mSessionPresenter?.sensorThresholdFor(mSessionPresenter?.selectedStream))
    }
}
