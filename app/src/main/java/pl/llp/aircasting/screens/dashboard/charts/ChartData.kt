package pl.llp.aircasting.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import com.google.common.collect.Lists
import org.apache.commons.lang3.time.DateUtils
import pl.llp.aircasting.lib.DateConverter
import pl.llp.aircasting.lib.TemperatureConverter
import pl.llp.aircasting.models.MeasurementStream
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.services.AveragedMeasurementsService
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
        // TODO: See if the ChartData instantiates only when following and unfollowing or at some other time as well
        initData()
        calculateAverages()
        calculateTimes()

        mChartRefreshService.setLastRefreshTime()
    }

    fun getEntries(stream: MeasurementStream?): List<Entry>? {
        return mEntriesPerStream[streamKey(stream)]
    }

    fun refresh(session: Session) {
        // TODO: This is wrong check. If session started at 9:59 the hour will change on 10:00 -> average will be calculated for one minute
        // TODO: We should probably have two different hourChanged values: One like this, when we have enough measurements (more than 9 hours)
        // TODO: And another one with check for elapsed hourChanged (from 9.59 - 10.59), when we have less than 9 hours of measurements
        var hourChanged = mSession.endTime?.hours != session.endTime?.hours
        mSession = session
        mEndTime = mSession.endTime ?: Date()
        calculateTimes()

        if(mChartRefreshService.shouldBeRefreshed() || hourChanged) {
            initData()
            calculateAverages()
            mChartRefreshService.setLastRefreshTime()
        }
    }

    private fun initData() {
        mEndTime = mSession.endTime ?: Date()
        mEntriesPerStream = HashMap()
        mMaxEntriesCount = 0
        mMeasurementStreams = initStreams()
    }

    private fun averageFrequency(): Int {
        return when (mSession.type) {
            Session.Type.MOBILE -> Calendar.MINUTE
            Session.Type.FIXED -> Calendar.HOUR
        }
    }

    private fun averagesCount(): Int {
        if (mMaxEntriesCount > 0) {
                return -mMaxEntriesCount + 1
            } else {
                return -mMaxEntriesCount
            }
    }

    private fun startTimeString(): String {
        val calendar = Calendar.getInstance()
        calendar.time = mEndTime
        calendar.add(averageFrequency(), averagesCount())
        val startString = DateConverter.get()?.toTimeStringForDisplay(timeToDisplay(calendar.time), TimeZone.getDefault())

        return startString ?: ""
    }

    private fun endTimeString(): String {
        val endString = DateConverter.get()?.toTimeStringForDisplay(timeToDisplay(mEndTime), TimeZone.getDefault())

        return endString ?: ""
    }

    private fun timeToDisplay(time: Date): Date {
        return when(mSession.type) {
            Session.Type.FIXED -> DateUtils.truncate(time, Calendar.HOUR)
            Session.Type.MOBILE -> time
        }
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

            entries?.toList()?.let {
                mEntriesPerStream.put(
                    streamKey(stream),
                    it
                )
            }
        }
    }

    private fun createEntries(stream: MeasurementStream?): MutableList<Entry>?{
        var entries: MutableList<Entry>? = null

        stream?.let { stream ->
            entries =  when (mSession.type) {
                Session.Type.MOBILE -> {
                    val averagedMeasurementsService = AveragedMeasurementsService(session.uuid)
                    val measurementsOverSecondThreshold = averagedMeasurementsService.getMeasurementsOverSecondThreshold(stream)
                    if (measurementsOverSecondThreshold.isNullOrEmpty()) {
                        ChartAveragesCreator().getMobileEntries(stream)
                    } else {
                        ChartAveragesCreator().getMobileEntriesForSessionOverSecondThreshold(measurementsOverSecondThreshold)
                    }
                }
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
