package pl.llp.aircasting.ui.view.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import pl.llp.aircasting.data.api.Constants
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.DateConverter
import java.util.*

class ExternalSessionChartDataCalculator(session: Session) : SessionChartDataCalculator(session) {

    override fun calculateEntriesAndTimestamps(stream: MeasurementStream?): MutableList<Entry>? {
        return when {
            stream == null -> null

            isFromOpenAQ(stream)
            -> OpenAQChartAveragesCreator().getFixedEntries(
                stream,
                this::setStartEndTimeToDisplay
            )

            else -> ChartAveragesCreator().getFixedEntries(
                stream,
                this::setStartEndTimeToDisplay
            )
        }
    }

    private fun isFromOpenAQ(stream: MeasurementStream) =
        stream.sensorName.contains(Constants.responseOpenAQSensorNamePM, true) ||
                stream.sensorName.contains(Constants.responseOpenAQSensorNameOzone, true)

    private fun setStartEndTimeToDisplay(start: Date, end: Date) {
        mStartTimeToDisplay = DateConverter.get()?.toTimeStringForDisplay(start) ?: ""
        mEndTimeToDisplay = DateConverter.get()?.toTimeStringForDisplay(end) ?: ""
    }
}