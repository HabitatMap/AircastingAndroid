package io.lunarlogic.aircasting.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import com.google.common.collect.Lists
import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Session
import java.util.*
import kotlin.collections.HashMap

class ChartData(
    var session: Session
) {
    var entriesStartTime: String = ""
    var entriesEndTime: String = ""

    private var mSession = session
    private lateinit var mEndTime: Date
    private lateinit var mEntriesPerStream: HashMap<String, List<Entry>>
    private var mMaxEntriesCount: Int = 0
    private lateinit var mMeasurementStreams: MutableList<MeasurementStream>
    private var mChartRefreshService = ChartRefreshService(session)

    init {
        initData()
        calculate()
        mChartRefreshService.setLastRefreshTime()
    }

    fun getEntries(stream: MeasurementStream?): List<Entry>? {
        return mEntriesPerStream[streamKey(stream)]
    }

    fun refresh(session: Session) {
        mSession = session
        initData()
        if(mChartRefreshService.shouldBeRefreshed()) {
            calculate()
            mChartRefreshService.setLastRefreshTime()
        }
    }

    private fun initData() {
        mEndTime = mSession.endTime ?: Date()
        mEntriesPerStream = HashMap()
        mMaxEntriesCount = 0
        mMeasurementStreams = initStreams()
    }

    private fun calculate() {
        calculateAverages()
        calculateTimes()
    }

    private fun averageFrequency(): Int {
        return when (mSession.type) {
            Session.Type.MOBILE -> Calendar.MINUTE
            Session.Type.FIXED -> Calendar.HOUR
        }
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

    private fun initStreams(): MutableList<MeasurementStream> {
        val streams: MutableList<MeasurementStream> = mutableListOf()
        mSession.streamsSortedByDetailedType().forEach { stream ->
            streams.add(stream)
        }
        return streams
    }

    private fun calculateAverages() {
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
            entries =  when (mSession.type) {
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
        return "${mSession.uuid}_${stream?.sensorName}"
    }
}
