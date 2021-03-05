package io.lunarlogic.aircasting.screens.session_view

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.SensorThreshold
import kotlinx.android.synthetic.main.activity_map.view.*
import kotlinx.android.synthetic.main.session_details_statistics_view.view.*
import java.util.*

class StatisticsContainer {
    private val mContext: Context

    private val mStatisticsView: View?

    private val mAvgValue: TextView?
    private val mNowValue: TextView?
    private val mPeakValue: TextView?

    private val mAvgCircleIndicator: ImageView?
    private val mNowCircleIndicator: ImageView?
    private val mPeakCircleIndicator: ImageView?

    private var mSensorThreshold: SensorThreshold? = null

    private var mSum: Double? = null
    private var mNow: Double? = null
    private var mPeak: Double? = null

    private var mVisibleTimeSpan: ClosedRange<Date>? = null

    constructor(rootView: View?, context: Context) {
        mContext = context

        mStatisticsView = rootView?.statistics_view

        mAvgValue = rootView?.avg_value
        mNowValue = rootView?.now_value
        mPeakValue = rootView?.peak_value

        mAvgCircleIndicator = rootView?.avg_circle_indicator
        mNowCircleIndicator = rootView?.now_circle_indicator
        mPeakCircleIndicator = rootView?.peak_circle_indicator
    }

    fun bindSession(sessionPresenter: SessionPresenter?) {
        val stream = sessionPresenter?.selectedStream
        mSensorThreshold = sessionPresenter?.selectedSensorThreshold()
        mVisibleTimeSpan = sessionPresenter?.visibleTimeSpan

        mStatisticsView?.visibility = View.VISIBLE

        bindLastMeasurement(sessionPresenter)
        bindAvgStatistics(stream)
        bindNowStatistics(stream)
        bindPeakStatistics(stream)
    }

    private fun bindLastMeasurement(sessionPresenter: SessionPresenter?) {
        val stream = sessionPresenter?.selectedStream

        mNow = getNowValue(stream)
        mSum?.let { mSum = it + (mNow ?: 0.0) }
        if (mPeak != null && mNow != null && mNow!! > mPeak!!) {
            mPeak = mNow
        }
    }

    fun refresh(sessionPresenter: SessionPresenter?) {
        mSum = null
        mPeak = null
        mNow = null
        bindSession(sessionPresenter)
    }

    private fun bindAvgStatistics(stream: MeasurementStream?) {
        var avg: Double? = null

        if (stream != null) {
            if (mSum == null) {
                mSum = stream.calculateSum()
            }

            val sum = if (mVisibleTimeSpan == null) {
                mSum!!
            } else {
                stream.calculateSum(mVisibleTimeSpan!!)
            }

            avg = sum / calculateMeasurementsSize(stream)
        }

        bindStatisticValues(stream, avg, mAvgValue, mAvgCircleIndicator)
    }

    private fun bindNowStatistics(stream: MeasurementStream?) {
        if (mNow == null && stream != null) {
            mNow = getNowValue(stream)
        }
        bindStatisticValues(stream, mNow, mNowValue, mNowCircleIndicator, true)
    }

    private fun bindPeakStatistics(stream: MeasurementStream?) {
        if (mPeak == null && stream != null) {
            mPeak = calculatePeak(stream)
        }

        val peak = if (mVisibleTimeSpan == null) {
            mPeak
        } else {
            stream?.let { calculatePeak(it) }
        }
        bindStatisticValues(stream, peak, mPeakValue, mPeakCircleIndicator)
    }

    private fun bindStatisticValues(stream: MeasurementStream?, value: Double?, valueView: TextView?, circleIndicator: ImageView?, isNow: Boolean = false) {
        valueView?.text = Measurement.formatValue(value)

        val color = MeasurementColor.forMap(mContext, value, mSensorThreshold)
        valueView?.background = StatisticsValueBackground(color, StatisticsValueBackground.RADIUS_BIG)
        circleIndicator?.setColorFilter(color)
    }

    private fun calculateMeasurementsSize(stream: MeasurementStream): Int {
        return streamMeasurements(stream).size
    }

    private fun calculatePeak(stream: MeasurementStream): Double {
        return streamMeasurements(stream).maxBy { it.value }?.value ?: 0.0
    }

    private fun streamMeasurements(stream: MeasurementStream): List<Measurement> {
        return if (mVisibleTimeSpan == null) {
            stream.measurements
        } else {
            stream.getMeasurementsForTimeSpan(mVisibleTimeSpan!!)
        }
    }
    private fun getNowValue(stream: MeasurementStream?): Double? {
        return stream?.measurements?.lastOrNull()?.value
    }
}
