package pl.llp.aircasting.ui.view.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import org.apache.commons.lang3.time.DateUtils
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.helpers.services.AveragedMeasurementsService
import java.util.*

class ChartData(
    var localSession: LocalSession
) {
    var entriesStartTime: String = ""
    var entriesEndTime: String = ""

    private var mSession = localSession
    private lateinit var mEndTime: Date
    private lateinit var mEntriesPerStream: HashMap<String, List<Entry>>
    private var mMaxEntriesCount: Int = 0
    private lateinit var mMeasurementStreams: MutableList<MeasurementStream>
    private var mChartRefreshService = ChartRefreshService(localSession)

    init {
        initData()
        calculateAverages()
        calculateTimes()

        mChartRefreshService.setLastRefreshTime()
    }

    fun getEntries(stream: MeasurementStream?): List<Entry>? {
        return mEntriesPerStream[streamKey(stream)]
    }

    fun refresh(localSession: LocalSession) {
        val hourChanged = mSession.endTime?.hours != localSession.endTime?.hours

        mSession = localSession
        mEndTime = mSession.endTime ?: Date()
        if(mChartRefreshService.isTimeToRefresh() || hourChanged) {
            initData()
            calculateAverages()
            calculateTimes()
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
            LocalSession.Type.MOBILE -> Calendar.MINUTE
            LocalSession.Type.FIXED -> Calendar.HOUR
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
            LocalSession.Type.FIXED -> DateUtils.truncate(time, Calendar.HOUR)
            LocalSession.Type.MOBILE -> time
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
                LocalSession.Type.MOBILE -> {
                    val averagedMeasurementsService = AveragedMeasurementsService(localSession.uuid)
                    val measurementsOverSecondThreshold = averagedMeasurementsService.getMeasurementsOverSecondThreshold(stream)
                    if (measurementsOverSecondThreshold.isNullOrEmpty()) {
                        ChartAveragesCreator().getMobileEntries(stream)
                    } else {
                        ChartAveragesCreator().getMobileEntriesForSessionOverSecondThreshold(measurementsOverSecondThreshold)
                    }
                }
                LocalSession.Type.FIXED -> ChartAveragesCreator().getFixedEntries(stream)
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
