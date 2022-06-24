package pl.llp.aircasting.ui.view.screens.dashboard.charts

import pl.llp.aircasting.data.model.MeasurementStream
import java.util.*

class OpenAQpmChartAveragesCreator : ChartAveragesCreator() {
    override fun modifyHours(date: Date, hours: Int): Date {
        return super.modifyHours(date, -2)
    }

    override fun getAllowedEndTimeBoundary(stream: MeasurementStream): Date {
        return stream.measurements.maxOf { it.time }
    }
}