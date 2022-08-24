package pl.llp.aircasting.ui.view.screens.dashboard.charts

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import pl.llp.aircasting.R

open class ChartConfigurator(private val mContext: Context) {
    open fun configure(chart: LineChart?, dataset: LineDataSet?) {
        // Horizontal grid and no Y Axis labels
        val rightYAxis = chart?.axisRight
        rightYAxis?.gridColor = ContextCompat.getColor(mContext, R.color.aircasting_grey_100)
        rightYAxis?.setDrawLabels(false)
        rightYAxis?.setDrawAxisLine(false)

        // Drawing grid even on an empty chart
        rightYAxis?.axisMinimum = 0f
        rightYAxis?.axisMaximum = 100f
        rightYAxis?.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)

        //Removing bottom "border" and Y values
        val leftYAxis = chart?.axisLeft
        leftYAxis?.gridColor = Color.TRANSPARENT
        leftYAxis?.setDrawAxisLine(false)
        leftYAxis?.setDrawLabels(false)

        // No labels on X Axis and no labels
        val xAxis = chart?.xAxis
        xAxis?.setDrawLabels(false)
        xAxis?.setDrawAxisLine(false)

        /* This is commented out, because we needed the chart to stretch with OpenAQ entries.
        * It's left just in case because there might be some glitches in the future,
        * which don't appear right away
        * */
        // Chart will not stretch even if there are less than 9 entries
        //xAxis?.spaceMin = (ChartAveragesCreator.MAX_AVERAGES_AMOUNT - mEntries.size).toFloat()

        // Removing vertical lines
        xAxis?.gridColor = Color.TRANSPARENT

        // Remove borders
        chart?.setDrawBorders(false)
        chart?.setBorderColor(Color.TRANSPARENT)

        // DATASET
        val lineData = LineData(dataset)

        // Formatting values on the chart (no decimal places)
        lineData.setValueFormatter(ChartValueFormatter())

        chart?.clear()
        chart?.data = lineData

        // Removing the legend for colors
        chart?.legend?.isEnabled = false

        // Removing description on the down right
        chart?.description?.isEnabled = false

        // Disabling chart scalability by pinching
        chart?.setScaleEnabled(false)

        // Disabling chart touch event
        chart?.setTouchEnabled(false)
    }
}