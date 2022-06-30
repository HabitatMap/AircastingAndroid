package pl.llp.aircasting.ui.view.screens.dashboard.charts

import android.content.Context
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineDataSet

class BottomSheetChartConfigurator(mContext: Context) : ChartConfigurator(mContext) {
    override fun configure(chart: LineChart?, dataset: LineDataSet?) {
        super.configure(chart, dataset)

        // Squeezing chart so that the max value does not jump to the top drastically
        chart?.apply {
            axisLeft.axisMinimum = -5f
            axisLeft.spaceTop = 80f
            axisRight.axisMinimum = -5f
            axisRight.spaceTop = 80f
        }
    }
}