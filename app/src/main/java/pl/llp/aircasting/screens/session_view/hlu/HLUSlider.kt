package pl.llp.aircasting.screens.session_view.hlu

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.slider.RangeSlider
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.TemperatureConverter
import pl.llp.aircasting.lib.labelFormat
import pl.llp.aircasting.lib.temperatureFromCelsiusToFahrenheit
import pl.llp.aircasting.lib.temperatureFromFahrenheitToCelsius
import pl.llp.aircasting.models.MeasurementStream
import pl.llp.aircasting.models.SensorThreshold

@SuppressLint("RestrictedApi")
class HLUSlider
    (
    rootView: View?,
    context: Context,
    onThresholdChanged: (sensorThreshold: SensorThreshold) -> Unit
) {
    private val mContext: Context = context

    private var mSegments: List<View?>
    private var fromLabel: TextView?
    private var toLabel: TextView?
    private val mLabels: List<TextView?>

    private val mSlider: RangeSlider?
    private val mThumbRadiusInPixels: Int

    private var mStream: MeasurementStream? = null
    private var mSensorThreshold: SensorThreshold? = null
    private var mOnThresholdChanged: (sensorThreshold: SensorThreshold) -> Unit = onThresholdChanged

    init {
        val lowSegment = rootView?.findViewById<View>(R.id.low_segment)
        val mediumSegment = rootView?.findViewById<View>(R.id.medium_segment)
        val highSegment = rootView?.findViewById<View>(R.id.high_segment)
        val veryHighSegment = rootView?.findViewById<View>(R.id.very_high_segment)
        mSegments = listOf(lowSegment, mediumSegment, highSegment, veryHighSegment)
        val lowLabel = rootView?.findViewById<TextView>(R.id.low_label)
        val mediumLabel = rootView?.findViewById<TextView>(R.id.medium_label)
        val highLabel = rootView?.findViewById<TextView>(R.id.high_label)
        fromLabel = rootView?.findViewById(R.id.very_low_label)
        toLabel = rootView?.findViewById(R.id.very_high_label)
        mLabels = listOf(lowLabel, mediumLabel, highLabel)
        mSlider = rootView?.findViewById(R.id.slider)
        mThumbRadiusInPixels =
            mContext.resources.getDimension(R.dimen.hlu_slider_thumb_radius).toInt()
        mSlider?.addOnChangeListener { _, _, _ ->
            draw()
        }
        mSlider?.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {
                // Do nothing
            }

            override fun onStopTrackingTouch(slider: RangeSlider) {
                updateSensorThreshold()
            }
        })
    }

    fun bindSensorThreshold(sensorThreshold: SensorThreshold?, stream: MeasurementStream? = null) {
        sensorThreshold ?: return

        mStream = stream
        mSensorThreshold = sensorThreshold

        setValuesForSliderBasedOnSelectedMeasurementStream()

        draw()
    }

    private fun setValuesForSliderBasedOnSelectedMeasurementStream() {
        mStream ?: return

        if (mStream?.measurementType == "Temperature" && TemperatureConverter.isCelsiusToggleEnabled()) mSlider?.apply {
            valueFrom =
                temperatureFromFahrenheitToCelsius(mSensorThreshold!!.from)
            valueTo =
                temperatureFromFahrenheitToCelsius(mSensorThreshold!!.to)

            values = arrayListOf(
                temperatureFromFahrenheitToCelsius(mSensorThreshold?.thresholdLow!!.toFloat()),
                temperatureFromFahrenheitToCelsius(mSensorThreshold?.thresholdMedium!!.toFloat()),
                temperatureFromFahrenheitToCelsius(mSensorThreshold?.thresholdHigh!!.toFloat())
            )
        } else mSlider?.apply {
            valueFrom = mSensorThreshold!!.from
            valueTo = mSensorThreshold!!.to
            values = arrayListOf(
                mSensorThreshold?.thresholdLow?.toFloat(),
                mSensorThreshold?.thresholdMedium?.toFloat(),
                mSensorThreshold?.thresholdHigh?.toFloat()
            )
        }
    }

    fun refresh(sensorThreshold: SensorThreshold?, stream: MeasurementStream? = null) {
        bindSensorThreshold(sensorThreshold, stream)
    }

    private fun updateSensorThreshold() {
        mSensorThreshold ?: return
        val values = mSlider?.values ?: return

        var thresholdLow = values.getOrNull(0)?.toInt()
        var thresholdMedium = values.getOrNull(1)?.toInt()
        var thresholdHigh = values.getOrNull(2)?.toInt()

        mStream ?: return
        if (mStream?.measurementType == "Temperature" && TemperatureConverter.isCelsiusToggleEnabled()) {
            thresholdLow = temperatureFromCelsiusToFahrenheit(thresholdLow!!)
            thresholdMedium = temperatureFromCelsiusToFahrenheit(thresholdMedium!!)
            thresholdHigh = temperatureFromCelsiusToFahrenheit(thresholdHigh!!)
        }

        thresholdLow?.let { mSensorThreshold?.thresholdLow = thresholdLow }
        thresholdMedium?.let { mSensorThreshold?.thresholdMedium = thresholdMedium }
        thresholdHigh?.let { mSensorThreshold?.thresholdHigh = thresholdHigh }

        mOnThresholdChanged.invoke(mSensorThreshold!!)
    }

    inner class SegmentProperty(adjacentProperty: SegmentProperty?, aValue: Float) {
        val xPosition: Float
        val width: Int
        val value: Float = aValue

        init {
            width = calculateWidth(adjacentProperty, value)
            xPosition = calculateXPosition(adjacentProperty)
        }

        private fun calculateXPosition(adjacentProperty: SegmentProperty?): Float {
            if (mSlider == null) return 0f

            return if (adjacentProperty == null) {
                mSlider.x
            } else {
                adjacentProperty.xPosition + adjacentProperty.width
            }
        }

        private fun calculateWidth(adjacentProperty: SegmentProperty?, value: Float): Int {
            if (mSlider == null || mSensorThreshold == null) return 0

            val adjacentValue = adjacentValue(adjacentProperty)
            val percentage =
                (value - adjacentValue) / (mSlider.valueTo - mSlider.valueFrom)
            return (percentage * mSlider.trackWidth).toInt()
        }

        private fun adjacentValue(adjacentProperty: SegmentProperty?): Float {
            return adjacentProperty?.value ?: mSlider?.valueFrom ?: 0f
        }
    }

    fun show() {
        mSegments.forEach { it?.visibility = View.VISIBLE }
        mLabels.forEach { it?.visibility = View.VISIBLE }
        fromLabel?.visibility = View.VISIBLE
        toLabel?.visibility = View.VISIBLE
        mSlider?.visibility = View.VISIBLE
    }

    /*
    This is the function for drawing the slider and setting values
    from threshold_very_low to threshold_very_high.
    */

    private fun draw() {
        mSensorThreshold ?: return
        val values = mSlider?.values ?: return

        var adjacentProperty: SegmentProperty? = null
        val segmentProperties = values.mapIndexed { index, value ->
            val segment = mSegments.getOrNull(index)
            val segmentProperty = SegmentProperty(adjacentProperty, value)
            updateSegmentSize(segment, segmentProperty.width)
            adjacentProperty = segmentProperty
            segmentProperty
        }

        fromLabel?.text = labelFormat(mSlider.valueFrom)
        toLabel?.text = labelFormat(mSlider.valueTo)
        mLabels.forEachIndexed { index, _ ->
            updateLabel(mLabels.getOrNull(index), segmentProperties.getOrNull(index))
        }
    }

    private fun updateSegmentSize(segment: View?, width: Int) {
        val params = LinearLayoutCompat.LayoutParams(segment?.layoutParams)
        params.width = width
        segment?.layoutParams = params
    }

    private fun updateLabel(label: TextView?, segmentProperty: SegmentProperty?) {
        if (label == null || segmentProperty == null) return

        val segmentEndPosition = segmentProperty.xPosition + segmentProperty.width
        val position = segmentEndPosition - mThumbRadiusInPixels

        label.x = position
        label.text = labelFormat(segmentProperty.value)
    }
}
