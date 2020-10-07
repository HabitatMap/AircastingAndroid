package io.lunarlogic.aircasting.screens.map

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.slider.RangeSlider
import io.lunarlogic.aircasting.R

class HLUSlider {
    private val mContext: Context

    private var mSegments: List<View?>
    private val mLabels: List<TextView?>

    private val mSlider: RangeSlider?
    private val mThumbRadiusInPixels: Int

    private var mFrom: Float
    private var mTo: Float

    constructor(rootView: View?, context: Context) {
        mContext = context

        val lowSegment = rootView?.findViewById<View>(R.id.low_segment)
        val mediumSegment = rootView?.findViewById<View>(R.id.medium_segment)
        val highSegment = rootView?.findViewById<View>(R.id.high_segment)
        val veryHighSegment = rootView?.findViewById<View>(R.id.very_high_segment)
        mSegments = listOf(lowSegment, mediumSegment, highSegment, veryHighSegment)

        val lowLabel = rootView?.findViewById<TextView>(R.id.low_label)
        val mediumLabel = rootView?.findViewById<TextView>(R.id.medium_label)
        val highLabel = rootView?.findViewById<TextView>(R.id.high_label)
        mLabels = listOf(lowLabel, mediumLabel, highLabel)

        mSlider = rootView?.findViewById(R.id.hlu_slider)

        mThumbRadiusInPixels = mContext.resources.getDimension(R.dimen.hlu_slider_thumb_radius).toInt()

        // TODO: get from settings/db
        mFrom = 0f
        mTo = 100f
        mSlider?.valueFrom = mFrom
        mSlider?.valueTo = mTo
        mSlider?.values = arrayListOf(5f, 80f, 90f)

        mSlider?.addOnChangeListener { _, _, _ ->
            draw()
        }
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
            if (mSlider == null) return 0

            val adjacentValue = adjacentValue(adjacentProperty)
            val percentage = (value - adjacentValue) / (mTo - mFrom)
            return (percentage * mSlider.trackWidth).toInt()
        }

        private fun adjacentValue(adjacentProperty: SegmentProperty?): Float {
            return adjacentProperty?.value ?: mFrom
        }
    }

    fun draw() {
        val values = mSlider?.values ?: return

        var adjacentProperty: SegmentProperty? = null
        val segmentProperties = values.mapIndexed { index, value ->
            val segment = mSegments.getOrNull(index)
            val segmentProperty = SegmentProperty(adjacentProperty, value)
            updateSegmentSize(segment, segmentProperty.width)
            adjacentProperty = segmentProperty
            segmentProperty
        }

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
        label.text = "%.0f".format(segmentProperty.value)
    }
}
