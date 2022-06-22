package pl.llp.aircasting.ui.view.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import pl.llp.aircasting.data.api.Constants
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.DateConverter
import java.util.*

class ExternalSessionChartDataCalculator(session: Session) : SessionChartDataCalculator(session) {

    override fun calculateEntriesAndTimestamps(stream: MeasurementStream?): MutableList<Entry>? {
        if (stream == null) return null

        if (streamIsFromAirBeam(stream))
            return super.calculateEntriesAndTimestamps(stream)

        return ExternalChartAveragesCreator().getFixedEntries(stream, this::setStartEndTime)
    }

    private fun streamIsFromAirBeam(stream: MeasurementStream) =
        stream.sensorName == Constants.responseAirbeam2SensorName
                || stream.sensorName == Constants.responseAirbeam3SensorName

    private fun setStartEndTime(start: Date, end: Date) {
        mStartTime = DateConverter.get()?.toTimeStringForDisplay(start) ?: ""
        mEndTime = DateConverter.get()?.toTimeStringForDisplay(end) ?: ""
    }
}