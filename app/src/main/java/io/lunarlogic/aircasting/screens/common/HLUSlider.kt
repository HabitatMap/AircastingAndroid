package io.lunarlogic.aircasting.screens.common

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.slider.RangeSlider
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.SensorThreshold

class HLUSlider {
    private val mContext: Context

    private var mSegments: List<View?>
    private var fromLabel: TextView?
    private var toLabel: TextView?
    private val mLabels: List<TextView?>

    private val mSlider: RangeSlider?
    private val mThumbRadiusInPixels: Int

    private var mSensorThreshold: SensorThreshold? = null
    private var mOnThresholdChanged: (sensorThreshold: SensorThreshold) -> Unit

    constructor(rootView: View?, context: Context, onThresholdChanged: (sensorThreshold: SensorThreshold) -> Unit) {
        mContext = context

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

        mThumbRadiusInPixels = mContext.resources.getDimension(R.dimen.hlu_slider_thumb_radius).toInt()

        mOnThresholdChanged = onThresholdChanged

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

    fun bindSensorThreshold(sensorThreshold: SensorThreshold?) {
        sensorThreshold ?: return

        mSensorThreshold = sensorThreshold
        mSlider?.valueFrom = sensorThreshold.from
        mSlider?.valueTo = sensorThreshold.to
        mSlider?.values = valuesFromThreshold(sensorThreshold)

        draw()
    }

    fun refresh(sensorThreshold: SensorThreshold?) {
        bindSensorThreshold(sensorThreshold)
    }

    private fun valuesFromThreshold(sensorThreshold: SensorThreshold): List<Float> {
        return arrayListOf(
            sensorThreshold.thresholdLow.toFloat(),
            sensorThreshold.thresholdMedium.toFloat(),
            sensorThreshold.thresholdHigh.toFloat()
        )
    }

    private fun updateSensorThreshold() {
        mSensorThreshold ?: return
        val values = mSlider?.values ?: return

        val thresholdLow = values.getOrNull(0)?.toInt()
        thresholdLow?.let { mSensorThreshold!!.thresholdLow = thresholdLow }
        val thresholdMedium = values.getOrNull(1)?.toInt()
        thresholdMedium?.let { mSensorThreshold!!.thresholdMedium = thresholdMedium }
        val thresholdHigh = values.getOrNull(2)?.toInt()
        thresholdHigh?.let { mSensorThreshold!!.thresholdHigh = thresholdHigh }

        mOnThresholdChanged.invoke(mSensorThreshold!!)
    }

    inner class SegmentProperty {
        val xPosition: Float
        val width: Int
        val value: Float

        constructor(adjacentProperty: SegmentProperty?, aValue: Float) {
            value = aValue
            width = calculateWidth(adjacentProperty, value)
            xPosition = calculateXPosition(adjacentProperty)
        }

        private fun calculateXPosition(adjacentProperty: SegmentProperty?): Float {
            if (mSlider == null) return 0f

            if (adjacentProperty == null) {
                return mSlider.x
            } else {
                return adjacentProperty.xPosition + adjacentProperty.width
            }
        }

        private fun calculateWidth(adjacentProperty: SegmentProperty?, value: Float): Int {
            if (mSlider == null || mSensorThreshold == null) return 0

            val adjacentValue = adjacentValue(adjacentProperty)
            val percentage = (value - adjacentValue) / (mSensorThreshold!!.to - mSensorThreshold!!.from)
            return (percentage * mSlider.trackWidth).toInt()
        }

        private fun adjacentValue(adjacentProperty: SegmentProperty?): Float {
            return adjacentProperty?.value ?: mSensorThreshold?.from ?: 0f
        }
    }

    fun show() {
        mSegments.forEach { it?.visibility = View.VISIBLE }
        mLabels.forEach { it?.visibility = View.VISIBLE }
        fromLabel?.visibility = View.VISIBLE
        toLabel?.visibility = View.VISIBLE
        mSlider?.visibility = View.VISIBLE
    }

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

        fromLabel?.text = labelFormat(mSensorThreshold?.from)
        toLabel?.text = labelFormat(mSensorThreshold?.to)
        mLabels.forEachIndexed { index, _ ->
            updateLabel(mLabels.getOrNull(index), segmentProperties.getOrNull(index))
        }
    }

    private fun updateSegmentSize(segment: View?, width: Int) {
        var params = LinearLayoutCompat.LayoutParams(segment?.layoutParams)
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

    private fun labelFormat(value: Float?): String {
        return "%d".format(value?.toInt())
    }
}
