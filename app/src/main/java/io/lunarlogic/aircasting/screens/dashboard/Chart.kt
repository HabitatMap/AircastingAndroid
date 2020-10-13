package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import android.graphics.Color
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
import kotlinx.android.synthetic.main.session_card.view.*


class Chart {
    private val mContext: Context
    private var mLineChart: LineChart?
    private val mRootView: View?
    private var mAverages: HashMap<String, List<Entry>> = HashMap();
//    private val mLayoutInflater: LayoutInflater

//    private var mSelectable: Boolean
//    private var mDisplayValues: Boolean

    private val mMeasurementStreams: MutableList<MeasurementStream> = mutableListOf()
    private var mCurrentStream: MeasurementStream? = null
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
        mLineChart = mRootView?.chart_view
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
            // check if there is any other way to pick default stream or pass strem from the view
            if(mCurrentStream == null) {
                mCurrentStream = session.streams.first()
            }
            println("MARYSIA: drawing chart for "+mCurrentStream?.sensorName+" session "+session.name)
            drawChart(mCurrentStream?.sensorName!!)
        }
    }

    private fun resetChart() {
        mLineChart?.data?.clearValues()
        mLineChart?.clear()
    }

    private fun bindMeasurements() {
        val session = mSessionPresenter?.session
        println("MARYSIA: all streams:")
        //if current stream is set we can bind averages only for that stream
        session?.streamsSortedByDetailedType()?.forEach { stream ->
            println("MARYSIA: "+stream.sensorName)
            bindStream(stream)
//            bindAverages(stream)
        }
        prepareCurrentEntries()
    }



    private fun onMeasurementClicked(stream: MeasurementStream) {
        resetChart()
        mOnMeasurementStreamChanged?.invoke(stream)
    }



    private fun bindAverages(stream: MeasurementStream) {
        println("MARYSIA: stream sensor name: "+stream.sensorName)
        drawChart(stream.sensorName)
    }

    private fun bindStream(stream: MeasurementStream) {
        mMeasurementStreams.add(stream)
    }

    private fun drawChart(sensorName: String) {
        val entries: List<Entry?>?
        val datasetLabel: String

         val rightYAxis = mLineChart?.axisRight
        rightYAxis?.gridColor = ContextCompat.getColor(mContext, R.color.aircasting_grey_100)
        rightYAxis?.setDrawLabels(false)
        rightYAxis?.setDrawAxisLine(false)

        //Removing bottom "border" and Y values
        val leftYAxis = mLineChart?.axisLeft
        leftYAxis?.gridColor = Color.TRANSPARENT
        leftYAxis?.setDrawAxisLine(false)
        leftYAxis?.setDrawLabels(false)

        val xAxis = mLineChart?.xAxis
        xAxis?.setDrawLabels(false)
        xAxis?.setDrawAxisLine(false)

        // Removing vertical lines
        xAxis?.gridColor = Color.TRANSPARENT


        mLineChart?.setDrawBorders(false)
        mLineChart?.setBorderColor(Color.TRANSPARENT)



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


        mLineChart?.clear()
        mLineChart?.data = lineData
        // Removing the legend for colors
        mLineChart?.legend?.isEnabled  = false

        // Removing description on the down right
        mLineChart?.description?.isEnabled = false

        // Refreshing the chart
        mLineChart?.invalidate()
    }

    private fun prepareCurrentEntries() {
        for (stream in mMeasurementStreams) {
            val sensorName: String = stream.sensorName
//            val streamKey: String = getKey(Constants.CURRENT_SESSION_FAKE_ID, sensorName)
            val entries: List<Entry>? = ChartAveragesCreator().getMobileEntries(stream)
            // probably key should be more unique than sensorName
            mAverages.put(
                sensorName,
                Lists.reverse(entries)
            )

//            setChartDataset(
//                sensorName
//            )

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
