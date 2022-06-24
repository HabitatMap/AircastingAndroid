package pl.llp.aircasting.ui.view.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import org.apache.commons.lang3.time.DateUtils
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.helpers.services.AveragedMeasurementsService
import java.util.*

open class SessionChartDataCalculator(private var mSession: Session) {
    interface OnAveragesCalculated {
        fun setStartEndTimeToDisplay(start: Date, end: Date)
    }
    var mStartTimeToDisplay: String = ""
    var mEndTimeToDisplay: String = ""
    protected val timeSetter = TimeSetter()
    lateinit var mEntriesPerStream: HashMap<String, List<Entry>>

    private var mMaxEntriesCount: Int = 0
    private lateinit var mMeasurementStreams: MutableList<MeasurementStream>

    init {
        initData()
        calculateData()
    }

    private fun initData() {
        mEntriesPerStream = HashMap()
        mMaxEntriesCount = 0
        mMeasurementStreams = initStreams()
    }

    fun refresh(session: Session) {
        mSession = session
        initData()
        calculateData()
    }

    private fun initStreams(): MutableList<MeasurementStream> {
        val streams: MutableList<MeasurementStream> = mutableListOf()
        mSession.streamsSortedByDetailedType().forEach { stream ->
            streams.add(stream)
        }
        return streams
    }

    private fun calculateData() {
        for (stream in mMeasurementStreams) {
            val entries: MutableList<Entry>? = calculateEntriesAndTimestamps(stream)
            val entriesSize = entries?.size ?: 0
            if (entriesSize > mMaxEntriesCount) {
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

    protected open fun calculateEntriesAndTimestamps(stream: MeasurementStream?): MutableList<Entry>? {
        var entries: MutableList<Entry>? = null

        stream?.let { stream ->
            when (mSession.type) {
                Session.Type.MOBILE -> {
                    val averagedMeasurementsService = AveragedMeasurementsService(mSession.uuid)
                    val measurementsOverSecondThreshold =
                        averagedMeasurementsService.getMeasurementsOverSecondThreshold(stream)

                    entries = if (measurementsOverSecondThreshold.isNullOrEmpty()) {
                        ChartAveragesCreator().getMobileEntries(stream)
                    } else {
                        ChartAveragesCreator().getMobileEntriesForSessionOverSecondThreshold(
                            measurementsOverSecondThreshold
                        )
                    }
                    calculateTimes()
                }
                Session.Type.FIXED -> entries = ChartAveragesCreator().getFixedEntries(stream, timeSetter)
            }
        }

        return entries
    }

    inner class TimeSetter : OnAveragesCalculated {
        override fun setStartEndTimeToDisplay(start: Date, end: Date) {
            mStartTimeToDisplay = DateConverter.get()?.toTimeStringForDisplay(start) ?: ""
            mEndTimeToDisplay = DateConverter.get()?.toTimeStringForDisplay(end) ?: ""
        }
    }

    private fun calculateTimes() {
        mStartTimeToDisplay = startTimeString()
        mEndTimeToDisplay = endTimeString()
    }

    private fun startTimeString(): String {
        val calendar = Calendar.getInstance()
        calendar.time = mSession.endTime ?: Date()
        calendar.add(averageFrequency(), averagesCount())
        val startString = DateConverter.get()
            ?.toTimeStringForDisplay(timeToDisplay(calendar.time), TimeZone.getDefault())

        return startString ?: ""
    }

    private fun endTimeString(): String {
        val endTime = mSession.endTime ?: Date()
        val endString = DateConverter.get()
            ?.toTimeStringForDisplay(timeToDisplay(endTime), TimeZone.getDefault())

        return endString ?: ""
    }

    private fun averageFrequency(): Int {
        return when (mSession.type) {
            Session.Type.MOBILE -> Calendar.MINUTE
            Session.Type.FIXED -> Calendar.HOUR
        }
    }

    private fun averagesCount(): Int {
        return if (mMaxEntriesCount > 0) {
            -mMaxEntriesCount + 1
        } else {
            -mMaxEntriesCount
        }
    }

    private fun timeToDisplay(time: Date): Date {
        return when (mSession.type) {
            Session.Type.FIXED -> DateUtils.truncate(time, Calendar.HOUR)
            Session.Type.MOBILE -> time
        }
    }

    fun streamKey(stream: MeasurementStream?): String {
        return "${mSession.uuid}_${stream?.sensorName}"
    }
}