package io.lunarlogic.aircasting.screens.map

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.screens.common.StatisticsValueBackground
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import kotlinx.android.synthetic.main.activity_map.view.*
import kotlinx.android.synthetic.main.map_statistics_view.view.*

class StatisticsContainer {
    private val mContext: Context

    private val mStatisticsView: View?

    private val mAvgLabel: TextView?
    private val mNowLabel: TextView?
    private val mPeakLabel: TextView?

    private val mAvgValue: TextView?
    private val mNowValue: TextView?
    private val mPeakValue: TextView?

    private val mAvgCircleIndicator: ImageView?
    private val mNowCircleIndicator: ImageView?
    private val mPeakCircleIndicator: ImageView?

    private var mSum: Double? = null
    private var mNow: Double? = null
    private var mPeak: Double? = null

    constructor(rootView: View?, context: Context) {
        mContext = context

        mStatisticsView = rootView?.statistics_view

        mAvgLabel = rootView?.avg_label
        mNowLabel = rootView?.now_label
        mPeakLabel = rootView?.peak_label

        mAvgValue = rootView?.avg_value
        mNowValue = rootView?.now_value
        mPeakValue = rootView?.peak_value

        mAvgCircleIndicator = rootView?.avg_circle_indicator
        mNowCircleIndicator = rootView?.now_circle_indicator
        mPeakCircleIndicator = rootView?.peak_circle_indicator
    }

    fun bindStream(stream: MeasurementStream?) {
        mStatisticsView?.visibility = View.VISIBLE

        mAvgLabel?.text = mContext.getString(R.string.avg_label, streamLabel(stream))
        mNowLabel?.text = mContext.getString(R.string.now_label, streamLabel(stream))
        mPeakLabel?.text = mContext.getString(R.string.peak_label, streamLabel(stream))

        bindAvgStatistics(stream)
        bindNowStatistics(stream)
        bindPeakStatistics(stream)
    }

    fun addMeasurement(measurement: Measurement) {
        mSum?.let { mSum = it + measurement.value }

        mNow = measurement.value

        if (mPeak != null && measurement.value > mPeak!!) {
            mPeak = measurement.value
        }
    }

    fun refresh(stream: MeasurementStream) {
        mSum = null
        mPeak = null
        mNow = null
        bindStream(stream)
    }

    private fun bindAvgStatistics(stream: MeasurementStream?) {
        if (stream == null) return

        if (mSum == null) {
            mSum = calculateSum(stream)
        }

        val avg = mSum!! / stream.measurements.size
        bindStatisticValues(stream, avg, mAvgValue, mAvgCircleIndicator)
    }

    private fun bindNowStatistics(stream: MeasurementStream?) {
        if (stream == null) return

        bindStatisticValues(stream, mNow, mNowValue, mNowCircleIndicator)
    }

    private fun bindPeakStatistics(stream: MeasurementStream?) {
        if (stream == null) return

        if (mPeak == null) {
            mPeak = calculatePeak(stream)
        }
        bindStatisticValues(stream, mPeak!!, mPeakValue, mPeakCircleIndicator)
    }

    private fun bindStatisticValues(stream: MeasurementStream, value: Double?, valueView: TextView?, circleIndicator: ImageView?) {
        valueView?.text = formatStatistic(value)

        val color = MeasurementColor.forMap(mContext, value, stream)
        valueView?.background = StatisticsValueBackground(color)
        circleIndicator?.setColorFilter(color)
    }

    private fun formatStatistic(value: Double?): String {
        if (value == null) {
            return "-"
        } else {
            return "%.0f".format(value)
        }
    }

    private fun calculateSum(stream: MeasurementStream): Double {
        return stream.measurements.sumByDouble { it.value }
    }

    private fun calculatePeak(stream: MeasurementStream): Double {
        return stream.measurements.maxBy { it.value }?.value ?: 0.0
    }

    private fun streamLabel(stream: MeasurementStream?): String? {
        if (stream == null) return null

        return stream.detailedType
    }
}
