package pl.llp.aircasting.ui.view.screens.dashboard.charts

import android.content.Context
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineDataSet

class BottomSheetChartConfigurator(mContext: Context) : ChartConfigurator(mContext) {
    override fun configure(chart: LineChart?, dataset: LineDataSet?) {

        val yMin = dataset?.yMin ?: 0f

        val xMax = dataset?.xMax ?: 0f
        val yMax = dataset?.yMax ?: 0f

        /**
         * Setting the chart's limits;
         * This way the chart will be drawn based on the xMax/yMax numbers that we're getting from the dataSet.
         * This will make the chart exactly the same as we have on IOS.
         **/
        chart?.apply {
            axisLeft.axisMinimum = -5f
            axisLeft.axisMaximum = yMax + yMax

            axisRight.axisMinimum = yMin
            axisRight.axisMaximum = xMax + xMax
        }
        super.configure(chart, dataset)
    }
}