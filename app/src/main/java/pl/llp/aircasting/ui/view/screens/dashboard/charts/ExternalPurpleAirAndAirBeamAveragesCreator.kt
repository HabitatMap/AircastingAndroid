package pl.llp.aircasting.ui.view.screens.dashboard.charts

import org.apache.commons.lang3.time.DateUtils
import pl.llp.aircasting.data.model.MeasurementStream
import java.util.*

/*
* This is used for external PurpleAir and AirBeam sessions.
* As their last hour of measurements is not complete, we cut off all the measurements from it.
*  */
class ExternalPurpleAirAndAirBeamAveragesCreator : ChartAveragesCreator() {
    override fun getAllowedEndTimeBoundary(stream: MeasurementStream): Date {
        val lastMeasurementTime = super.getAllowedEndTimeBoundary(stream)
        val lastMeasurementHour = DateUtils.truncate(lastMeasurementTime, Calendar.HOUR_OF_DAY)
        return Date(lastMeasurementHour.time - 1)
    }

    override fun modifyHours(date: Date, hours: Int): Date {
        return super.modifyHours(date, -1)
    }
}