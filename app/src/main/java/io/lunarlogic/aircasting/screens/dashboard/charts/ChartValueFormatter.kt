package io.lunarlogic.aircasting.screens.dashboard.charts

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat

class ChartValueFormatter: ValueFormatter() {
        private val format = DecimalFormat("###,##0")

        override fun getPointLabel(entry: Entry?): String {
            return format.format(entry?.y)
        }
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return format.format(value)
        }
}
