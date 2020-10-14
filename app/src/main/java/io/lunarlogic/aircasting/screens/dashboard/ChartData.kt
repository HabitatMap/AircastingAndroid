package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.sensor.Session
import com.github.mikephil.charting.data.Entry
import com.google.common.collect.Lists
import io.lunarlogic.aircasting.sensor.MeasurementStream

class ChartData(
    var session: Session
) {
    private var mEntriesPerStream: HashMap<String, List<Entry>> = HashMap()
    private var mMeasurementStreams: MutableList<MeasurementStream> = initStreams()

    init {
        calculateAvarages()
    }

    fun getEntries(stream: MeasurementStream?): List<Entry>? {
        return mEntriesPerStream[streamKey(stream)]
    }

    private fun initStreams(): MutableList<MeasurementStream> {
        var streams: MutableList<MeasurementStream> = mutableListOf()
        session.streamsSortedByDetailedType()?.forEach { stream ->
            streams.add(stream)
        }
        return streams
    }

    private fun calculateAvarages() {
        for (stream in mMeasurementStreams) {
            val entries: List<Entry>? = ChartAveragesCreator().getMobileEntries(stream)
            mEntriesPerStream.put(
                streamKey(stream),
                Lists.reverse(entries)
            )
        }
    }

    private fun streamKey(stream: MeasurementStream?): String {
        return "${session.uuid}_${stream?.sensorName}"
    }
}
