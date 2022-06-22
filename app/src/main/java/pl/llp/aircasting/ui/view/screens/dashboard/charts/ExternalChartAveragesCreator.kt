package pl.llp.aircasting.ui.view.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import org.apache.commons.lang3.time.DateUtils
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import java.util.*

class ExternalChartAveragesCreator : ChartAveragesCreator() {
    override fun getFixedEntries(
        stream: MeasurementStream,
        setStartEndTimeCallback: ((startTime: Date, endTime: Date) -> Unit)?
    ): MutableList<Entry> {
        if (stream.measurements.isEmpty()) return mutableListOf()

        val calendar = Calendar.getInstance()
        setAllowedTimeLimitToCalendar(stream, calendar)

        val measurements = getMeasurementsAfterAllowedTimeLimit(stream, calendar)
        var numberOfDots = MIN_X_VALUE
        val entries: MutableList<Entry> = mutableListOf()

        if (measurements.isEmpty()) return entries

        val periodData = groupMeasurementsByHours(measurements)
        if (periodData.isNotEmpty()) {
            // From time to time we still get 10 entries, so this is another check
            val lastNineHoursMeasurementGroups = periodData.entries.toList().takeLast(9)
            val firstEntry = lastNineHoursMeasurementGroups[0]

            for (dataChunk in lastNineHoursMeasurementGroups) {
                if (numberOfDots > MAX_AVERAGES_AMOUNT) return entries

                val yValue = getAverage(dataChunk.value).toFloat()
                val xValue = getXvalueBasedOnTimeDifference(dataChunk, firstEntry)
                entries.add(
                    Entry(
                        xValue,
                        yValue
                    )
                )
                numberOfDots++
            }

            if (setStartEndTimeCallback != null) {
                setStartEndTimeCallback(firstEntry.key, lastNineHoursMeasurementGroups.last().key)
            }
        }
        return entries
    }

    private fun getXvalueBasedOnTimeDifference(
        dataChunk: Map.Entry<Date, List<Measurement>>,
        firstEntry: Map.Entry<Date, List<Measurement>>
    ): Float {
        return ((dataChunk.key.time - firstEntry.key.time) / 1000 / 3600).toFloat()
    }

    private fun getMeasurementsAfterAllowedTimeLimit(
        stream: MeasurementStream,
        boundary: Calendar
    ) = stream.measurements.sortedBy { it.time }.filter { it.time > boundary.time }

    private fun setAllowedTimeLimitToCalendar(
        stream: MeasurementStream,
        calendar: Calendar
    ) {
        val latestTime = stream.measurements.maxOf { it.time }
        calendar.time = latestTime
        calendar.add(Calendar.HOUR_OF_DAY, -9)
    }

    private fun groupMeasurementsByHours(
        measurements: List<Measurement>,
    ) = measurements.groupBy {
        DateUtils.truncate(it.time, Calendar.HOUR_OF_DAY)
    }
}