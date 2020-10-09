package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.common.collect.Lists
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.sensor.MeasurementStream
import java.text.DecimalFormat


class Chart {
    private val mContext: Context
    private var mLineChart: LineChart
    private val mRootView: View?
    private var mAverages: HashMap<String, List<Entry>> = HashMap();
//    private val mLayoutInflater: LayoutInflater

//    private var mSelectable: Boolean
//    private var mDisplayValues: Boolean

    private val mMeasurementStreams: MutableList<MeasurementStream> = mutableListOf()
//    private val mLastMeasurementColors: HashMap<MeasurementStream, Int> = HashMap()

//    private val mMeasurementsTable: TableLayout?
//    private val mMeasurementHeaders: TableRow?
//    private var mMeasurementValues: TableRow? = null

//    private val mHeaderColor: Int
//    private val mSelectedHeaderColor: Int

    private var mSessionPresenter: SessionPresenter? = null
    private var mOnMeasurementStreamChanged: ((MeasurementStream) -> Unit)? = null

    constructor(
        context: Context,
        rootView: View?

    ) {
        mContext = context
        mRootView = rootView
        mLineChart = mRootView?.chart
    }


    fun refresh() {
        bindSession(mSessionPresenter, mOnMeasurementStreamChanged)
    }

    fun bindSession(
        sessionPresenter: SessionPresenter?,
        onMeasurementStreamChanged: ((MeasurementStream) -> Unit)? = null
    ) {
        mSessionPresenter = sessionPresenter
        mOnMeasurementStreamChanged = onMeasurementStreamChanged

        val session = mSessionPresenter?.session
        if (session != null && session.streams.count() > 0) {
            resetChart()
            bindMeasurements()
        }
    }

    private fun resetChart() {
        // assign new data
        // invalidate chart
    }

    private fun bindMeasurements() {
        val session = mSessionPresenter?.session
        session?.streamsSortedByDetailedType()?.forEach { stream ->
//            bindStream(stream)
            bindAverages(stream)
        }
    }



    private fun onMeasurementClicked(stream: MeasurementStream) {
        resetChart()
        mOnMeasurementStreamChanged?.invoke(stream)
    }



    private fun bindAverages(stream: MeasurementStream) {
        setChartDataset(stream.sensorName)


    }

    private fun setChartDataset(sensorName: String) {
        val entries: List<Entry?>?
        val datasetLabel: String

        prepareCurrentEntries()
        entries = getEntriesForStream(sensorName)

        if (entries == null || entries.isEmpty()) {
            return
        }

        val dataSet = prepareDataSet(entries, sensorName)
        val lineData: LineData = LineData(dataSet)

        // Formatting values on the chart (no decimal places)
        val formatter: ValueFormatter = object : ValueFormatter() {
            private val format = DecimalFormat("###,##0")
            override fun getPointLabel(entry: Entry?): String {
                return format.format(entry?.y)
            }
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return format.format(value)
            }
        }
        lineData.setValueFormatter(formatter)


        mLineChart.clear()
        mLineChart.data = lineData
        // Removing the legend for colors
        mLineChart.legend.isEnabled  = false

        // Removing description on the down right
        mLineChart.description.isEnabled = false

        // Refreshing the chart
        mLineChart.invalidate()
    }

    private fun prepareCurrentEntries() {
        for (stream in mMeasurementStreams) {
            val sensorName: String = stream.sensorName
//            val streamKey: String = getKey(Constants.CURRENT_SESSION_FAKE_ID, sensorName)
            val entries: List<Entry>? = ChartAveragesCreator().getMobileEntries(stream)
            mAverages.put(
                sensorName,
                Lists.reverse(entries)
            )

            setChartDataset(
                sensorName
            )

        }
    }

    private fun prepareDataSet(entries: List<Entry?>?, sensorName: String): LineDataSet {
        val dataSet: LineDataSet = LineDataSet(entries, "")

        // Making the line a curve, not a polyline
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        // Circle colors
        dataSet.circleRadius = 7f
        dataSet.setCircleColors(
            ContextCompat.getColor(
                mContext,
                R.color.session_color_indicator_low_shadow
            )
        )
        dataSet.fillAlpha = 10
        dataSet.circleHoleRadius = 3.5f
        dataSet.circleHoleColor = ContextCompat.getColor(
            mContext,
            R.color.session_color_indicator_low
        )

        // Line color
        dataSet.setColor(ContextCompat.getColor(mContext, R.color.aircasting_grey_300))
        dataSet.lineWidth = 1f

        return dataSet
    }

    private fun getEntriesForStream(sensorName: String): List<Entry?>? {
        return mAverages.get(sensorName)
    }
}
