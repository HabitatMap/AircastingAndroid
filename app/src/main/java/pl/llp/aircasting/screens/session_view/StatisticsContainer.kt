package pl.llp.aircasting.screens.session_view

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import pl.llp.aircasting.lib.MeasurementColor
import pl.llp.aircasting.models.Measurement
import pl.llp.aircasting.models.MeasurementStream
import pl.llp.aircasting.models.SensorThreshold
import pl.llp.aircasting.screens.dashboard.SessionPresenter
import kotlinx.android.synthetic.main.activity_map.view.*
import kotlinx.android.synthetic.main.session_details_statistics_view.view.*
import pl.llp.aircasting.lib.TemperatureConverter
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

    private var mMeasurementsValues: MutableList<Double?>? = null
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

        if (stream?.measurementType == "Temperature") {
            TemperatureConverter.setAppropriateDetailedType(stream)
        }

        mSensorThreshold = sessionPresenter?.selectedSensorThreshold()
        mVisibleTimeSpan = sessionPresenter?.visibleTimeSpan

        if (stream != null) {
            mStatisticsView?.visibility = View.VISIBLE
        }

        bindLastMeasurement(sessionPresenter)
        bindAvgStatistics(stream)
        bindNowStatistics(stream)
        bindPeakStatistics(stream)
    }

    private fun bindLastMeasurement(sessionPresenter: SessionPresenter?) {
        val stream = sessionPresenter?.selectedStream

        mNow = getNowValue(stream)

        setMeasurementsValues(stream)

        mMeasurementsValues?.add(mNow)

        if (mPeak != null && mNow != null && mNow!! > mPeak!!) {
            mPeak = mNow
        }
    }

    fun refresh(sessionPresenter: SessionPresenter?) {
        mMeasurementsValues = null
        mPeak = null
        mNow = null
        bindSession(sessionPresenter)
    }

    private fun bindAvgStatistics(stream: MeasurementStream?) {
        var avg: Double? = null

        if (stream != null) {
            val sum = calculateSum(stream)

            avg = sum / calculateMeasurementsSize(stream)

            if (stream.measurementType == "Temperature")
                avg = TemperatureConverter.getAppropriateTemperatureValue(avg)
        }

        bindStatisticValues(stream, avg, mAvgValue, mAvgCircleIndicator)
    }

    private fun calculateSum(stream: MeasurementStream?): Double {
        if (stream == null) return 0.0
        val values = streamMeasurementsValues(stream)

        return values?.sum() ?: 0.0
    }

    private fun bindNowStatistics(stream: MeasurementStream?) {
        if (mNow == null && stream != null) {
            mNow = getNowValue(stream)

        }
        if (stream?.measurementType == "Temperature")
            mNow = mNow?.let { TemperatureConverter.getAppropriateTemperatureValue(it) }

        bindStatisticValues(stream, mNow, mNowValue, mNowCircleIndicator, StatisticsValueBackground.RADIUS_BIG)
    }

    private fun bindPeakStatistics(stream: MeasurementStream?) {
        if (mPeak == null && stream != null) {
            mPeak = calculatePeak(stream)
        }

        var peak = if (mVisibleTimeSpan == null) {
            mPeak
        } else {
            stream?.let { calculatePeak(it) }
        }

        if (stream?.measurementType == "Temperature")
            peak = peak?.let { TemperatureConverter.getAppropriateTemperatureValue(it) }

        bindStatisticValues(stream, peak, mPeakValue, mPeakCircleIndicator)
    }

    private fun bindStatisticValues(stream: MeasurementStream?, value: Double?, valueView: TextView?, circleIndicator: ImageView?, radius: Float = StatisticsValueBackground.CORNER_RADIUS) {
        valueView?.text = Measurement.formatValue(value)

        val color = MeasurementColor.forMap(mContext, value, mSensorThreshold)
        valueView?.background = StatisticsValueBackground(color, radius)
        circleIndicator?.setColorFilter(color)
    }

    private fun calculateMeasurementsSize(stream: MeasurementStream): Int {
        return streamMeasurementsValues(stream)?.size ?: 0
    }

    private fun calculatePeak(stream: MeasurementStream): Double {
        val values = streamMeasurementsValues(stream)
        return values?.maxOrNull() ?: 0.0
    }

    private fun streamMeasurementsValues(stream: MeasurementStream): List<Double>? {
        val measurementsValues = if (mVisibleTimeSpan != null) {
            measurementsValuesForSpan(stream, mVisibleTimeSpan!!)
        } else {
            setMeasurementsValues(stream)
            mMeasurementsValues
        }
        return measurementsValues?.filterNotNull()
    }

    private fun setMeasurementsValues(stream: MeasurementStream?) {
        if (mMeasurementsValues == null) {
            mMeasurementsValues =  measurementsValues(stream)
        }
    }

    private fun measurementsValues(stream: MeasurementStream?): MutableList<Double?> {
        if (stream == null) return mutableListOf()

        return stream.measurements.map { it.value }.toMutableList()
    }

    private fun measurementsValuesForSpan(stream: MeasurementStream?, visibleTimeSpan: ClosedRange<Date>): List<Double> {
        if (stream == null) return listOf()

        return stream.getMeasurementsForTimeSpan(visibleTimeSpan).map { it.value }
    }

    private fun getNowValue(stream: MeasurementStream?): Double? {
        return stream?.measurements?.lastOrNull()?.value
    }
}
