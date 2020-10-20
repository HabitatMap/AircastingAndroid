package io.lunarlogic.aircasting.screens.dashboard

import com.github.mikephil.charting.data.Entry
import com.google.common.collect.Lists
import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session
import java.util.*
import kotlin.collections.HashMap

class ChartData(
    var session: Session
) {
    var entriesStartTime: String = ""
    var entriesEndTime: String = ""

    private val mEndTime: Date = session.endTime ?: Date()
    private var mEntriesPerStream: HashMap<String, List<Entry>> = HashMap()
    private var mMaxEntriesCount: Int = 0
    private var mMeasurementStreams: MutableList<MeasurementStream> = initStreams()
    init {
        calculateAvarages()
        calculateTimes()
    }

    fun getEntries(stream: MeasurementStream?): List<Entry>? {
        return mEntriesPerStream[streamKey(stream)]
    }

    private fun startTimeString(): String {
        val calendar = Calendar.getInstance()
        calendar.time = mEndTime
        calendar.add(averageFrequency(), -mMaxEntriesCount)
        val startString = DateConverter.toDateString(calendar.time, TimeZone.getDefault(), "HH:mm")
        return startString
    }

    private fun endTimeString(): String {
        val endString = DateConverter.toDateString(mEndTime, TimeZone.getDefault(), "HH:mm")
        return endString
    }

    private fun averageFrequency(): Int {
        return when (session.type) {
            Session.Type.MOBILE -> Calendar.MINUTE
            Session.Type.FIXED -> Calendar.HOUR
        }
    }
    private fun initStreams(): MutableList<MeasurementStream> {
        val streams: MutableList<MeasurementStream> = mutableListOf()
        session.streamsSortedByDetailedType().forEach { stream ->
            streams.add(stream)
        }
        return streams
    }

    private fun calculateAvarages() {
        for (stream in mMeasurementStreams) {
            val entries: MutableList<Entry>? = createEntries(stream)
            val entriesSize = entries?.size ?: 0
            if(entriesSize > mMaxEntriesCount) {
                mMaxEntriesCount = entries?.size ?: 0
            }

            mEntriesPerStream.put(
                streamKey(stream),
                Lists.reverse(entries)
            )
        }
    }

    private fun createEntries(stream: MeasurementStream?): MutableList<Entry>?{
        var entries: MutableList<Entry>? = null

        stream?.let { stream ->
            entries =  when (session.type) {
                Session.Type.MOBILE -> ChartAveragesCreator().getMobileEntries(stream)
                Session.Type.FIXED -> ChartAveragesCreator().getFixedEntries(stream)
            }
        }

        return entries
    }

    private fun calculateTimes() {
        entriesStartTime = startTimeString()
        entriesEndTime = endTimeString()
    }

    private fun streamKey(stream: MeasurementStream?): String {
        return "${session.uuid}_${stream?.sensorName}"
    }
}
