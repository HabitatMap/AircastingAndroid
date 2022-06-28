package pl.llp.aircasting.ui.view.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import pl.llp.aircasting.data.api.util.StringConstants
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session

class ExternalSessionChartDataCalculator(session: Session) : SessionChartDataCalculator(session) {

    override fun calculateEntriesAndTimestamps(stream: MeasurementStream?): MutableList<Entry>? {
        return when {
            stream == null -> null

            isFromOpenAQ(stream) -> OpenAQChartAveragesCreator().getFixedEntries(
                stream,
                timeSetter
            )

            else -> ChartAveragesCreator().getFixedEntries(
                stream,
                timeSetter
            )
        }
    }

    private fun isFromOpenAQ(stream: MeasurementStream) =
        stream.sensorName.contains(StringConstants.responseOpenAQSensorNamePM, true) ||
                stream.sensorName.contains(StringConstants.responseOpenAQSensorNameOzone, true)
}