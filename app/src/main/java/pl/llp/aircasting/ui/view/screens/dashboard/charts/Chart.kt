package pl.llp.aircasting.ui.view.screens.dashboard.charts

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.expanded_session_view.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.MeasurementColor
import pl.llp.aircasting.util.TemperatureConverter
import java.util.*

class Chart(
    context: Context,
    rootView: View?,
    private val configurator: ChartConfigurator = ChartConfigurator(context)
) {
    private val mContext: Context = context
    private val mRootView: View? = rootView
    private val mChartStartTimeTextView: TextView? = mRootView?.chart_start_time
    private val mChartEndTimeTextView: TextView? = mRootView?.chart_end_time
    private val mChartUnitTextView: TextView? = mRootView?.chart_unit

    private var mEntries: List<Entry> = listOf()

    private var mLineChart: LineChart? = mRootView?.chart_view
    private var mDataSet: LineDataSet? = null
    private var mSessionPresenter: SessionPresenter? = null

    fun bindChart(sessionPresenter: SessionPresenter?) {
        mSessionPresenter = sessionPresenter

        setEntries()
        resetChart()
        prepareDataSet()
        drawChart()
        setTimesAndUnit()
    }

    private fun setEntries() {
        mEntries = mSessionPresenter?.chartData?.getEntries(mSessionPresenter?.selectedStream)
            ?: listOf()
    }

    private fun resetChart() {
        mLineChart?.data?.clearValues()
        mLineChart?.clear()
    }

    private fun prepareDataSet() {
        if (mEntries.isEmpty()) {
            mDataSet = LineDataSet(listOf(), "")
            return
        }

        val dataSet: LineDataSet =
            if (mSessionPresenter?.selectedStream?.isMeasurementTypeTemperature() == true
                && TemperatureConverter.isCelsiusToggleEnabled()
            ) {
                val celsiusEntries: List<Entry> = mEntries.map { entry ->
                    Entry(entry.x, TemperatureConverter.fahrenheitToCelsius(entry.y))
                }
                LineDataSet(celsiusEntries, "")
            } else LineDataSet(mEntries, "")

        // Making the line a curve, not a polyline
        dataSet.mode = LineDataSet.Mode.LINEAR

        // Circle colors
        dataSet.circleRadius = 3.5f
        dataSet.circleColors = circleColors()
        dataSet.setDrawCircleHole(false)

        // Line color
        dataSet.color = ContextCompat.getColor(mContext, R.color.aircasting_grey_300)
        dataSet.lineWidth = 1f

        // Values size and color
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = ContextCompat.getColor(mContext, R.color.aircasting_grey_700)

        mDataSet = dataSet
    }

    private fun drawChart() {
        configurator.configure(mLineChart, mDataSet)

        // Refreshing the chart
        mLineChart?.invalidate()
    }

    private fun setTimesAndUnit() {
        val session = mSessionPresenter?.session
        session ?: return

        DateConverter.get()?.apply {
            val mStartTime = toTimeStringForDisplay(session.startTime, TimeZone.getDefault())
            val mEndTime = toTimeStringForDisplay(session.endTime ?: Date(), TimeZone.getDefault())

            val entriesStartTime = mSessionPresenter?.chartData?.entriesStartTime

            mChartStartTimeTextView?.text = entriesStartTime ?: mStartTime
            mChartEndTimeTextView?.text = mEndTime
            mChartUnitTextView?.text = chartUnitText()
        }
    }

    private fun circleColors(): List<Int> {
        return mEntries.map { entry ->
            getColor(entry.y)
        }
    }

    private fun getColor(value: Float?): Int {
        val measurementValue = value?.toDouble() ?: 0.0
        return MeasurementColor.forMap(
            mContext,
            measurementValue,
            mSessionPresenter?.sensorThresholdFor(mSessionPresenter?.selectedStream)
        )
    }

    private fun chartUnitText(): String {
        return "${mContext.getString(chartUnitLabelId())} - ${mSessionPresenter?.selectedStream?.unitSymbol}"
    }

    private fun chartUnitLabelId(): Int {
        return if (mSessionPresenter?.isFixed()!!) {
            R.string.fixed_session_units_label
        } else {
            R.string.mobile_session_units_label
        }
    }
}
