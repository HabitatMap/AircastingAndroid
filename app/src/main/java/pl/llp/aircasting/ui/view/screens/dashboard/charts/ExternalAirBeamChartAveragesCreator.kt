package pl.llp.aircasting.ui.view.screens.dashboard.charts

import org.apache.commons.lang3.time.DateUtils
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import java.util.*

class ExternalAirBeamChartAveragesCreator : ExternalChartAveragesCreator() {
    override fun getAllowedEndTimeBoundary(stream: MeasurementStream): Date {
        val lastMeasurementTime = super.getAllowedEndTimeBoundary(stream)
        return DateUtils.truncate(lastMeasurementTime, Calendar.HOUR_OF_DAY)
    }

    override fun getMeasurementsInAllowedTimeBoundaries(
        stream: MeasurementStream,
        boundary: Calendar
    ): List<Measurement> {
        val withStartTimeBoundary = super.getMeasurementsInAllowedTimeBoundaries(stream, boundary)
        return withStartTimeBoundary.filter { it.time < getAllowedEndTimeBoundary(stream) }
    }

    override fun modifyHours(date: Date, hours: Int): Date {
        return super.modifyHours(date, -1)
    }
}