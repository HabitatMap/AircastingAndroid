package pl.llp.aircasting.ui.view.screens.dashboard.charts

import pl.llp.aircasting.data.model.MeasurementStream
import java.util.*
/*
* This is used for OpenAQ sessions.
* As they provide one measurement per hour as already averaged one,
* We include the last hour and we don't cut it off
*  */
class OpenAQChartAveragesCreator : ChartAveragesCreator() {
    override fun modifyHours(date: Date, hours: Int): Date {
        return super.modifyHours(date, -2)
    }

    override fun getAllowedEndTimeBoundary(stream: MeasurementStream): Date {
        return stream.measurements.maxOf { it.time }
    }
}