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
import pl.llp.aircasting.util.MeasurementColor
import pl.llp.aircasting.util.TemperatureConverter
import pl.llp.aircasting.util.extensions.inVisible

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
        val session = sessionPresenter?.session
        mSessionPresenter = sessionPresenter

        setEntries(sessionPresenter)

        if (session != null && session.streams.isNotEmpty()) {
            resetChart()
            mDataSet = prepareDataSet()
            drawChart()
        }
        setTimesAndUnit()
    }

    private fun resetChart() {
        mLineChart?.data?.clearValues()
        mLineChart?.clear()
    }

    private fun setEntries(sessionPresenter: SessionPresenter?) {
        mEntries = sessionPresenter?.chartData?.getEntries(sessionPresenter.selectedStream)
            ?: listOf()
    }

    private fun drawChart() {
        configurator.configure(mLineChart, mDataSet)
        // Refreshing the chart
        mLineChart?.invalidate()
    }

    private fun prepareDataSet(): LineDataSet {
        if (mEntries.isEmpty()) return LineDataSet(listOf(), "")

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

        return dataSet
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

    private fun setTimesAndUnit() {
        if (mSessionPresenter?.selectedStream == null) {
            mChartStartTimeTextView?.inVisible()
            mChartEndTimeTextView?.inVisible()
            mChartUnitTextView?.inVisible()
        } else {
            mChartStartTimeTextView?.text = mSessionPresenter?.chartData?.entriesStartTime
            mChartEndTimeTextView?.text = mSessionPresenter?.chartData?.entriesEndTime
            mChartUnitTextView?.text = chartUnitText()
        }
    }

    private fun chartUnitText(): String {
        return "${mContext.getString(chartUnitLabelId())} - ${mSessionPresenter?.selectedStream?.detailedType}"
    }

    private fun chartUnitLabelId(): Int {
        return if (mSessionPresenter?.isFixed()!!) {
            R.string.fixed_session_units_label
        } else {
            R.string.mobile_session_units_label
        }
    }
}
