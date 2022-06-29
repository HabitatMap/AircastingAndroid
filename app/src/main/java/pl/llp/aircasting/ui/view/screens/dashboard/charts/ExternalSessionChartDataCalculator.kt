package pl.llp.aircasting.ui.view.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import pl.llp.aircasting.data.api.util.StringConstants
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import java.util.*

class ExternalSessionChartDataCalculator(session: Session) : SessionChartDataCalculator(session) {
    override fun calculateEntriesAndTimestamps(stream: MeasurementStream?): MutableList<Entry>? {
        val timeStampsSetter = UTCTimeStampsSetter()

        return when {
            stream == null -> null

            isFromOpenAQ(stream) -> OpenAQChartAveragesCreator().getFixedEntries(
                stream,
                timeStampsSetter
            )

            else -> ChartAveragesCreator().getFixedEntries(
                stream,
                timeStampsSetter
            )
        }
    }

    private fun isFromOpenAQ(stream: MeasurementStream) =
        stream.sensorName.contains(StringConstants.responseOpenAQSensorNamePM, true) ||
                stream.sensorName.contains(StringConstants.responseOpenAQSensorNameOzone, true)

    inner class UTCTimeStampsSetter : TimeStampsSetter() {
        override fun setStartEndTimeToDisplay(start: Date, end: Date, timeZone: TimeZone) {
            super.setStartEndTimeToDisplay(start, end, TimeZone.getTimeZone("UTC"))
        }
    }
}