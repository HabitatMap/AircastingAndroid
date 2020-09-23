package io.lunarlogic.aircasting.screens.dashboard

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.screens.common.SelectedSensorBorder
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session

abstract class ActiveSessionViewMvcImpl<ListenerType>: SessionViewMvcImpl<ListenerType> {
    private val mMeasurementValues: TableRow

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup,
        supportFragmentManager: FragmentManager
    ): super(inflater, parent, supportFragmentManager) {
        mMeasurementValues = findViewById(R.id.measurement_values)
    }

    override fun layoutId(): Int {
        return R.layout.active_session
    }

    override fun bindSession(session: Session) {
        bindSessionDetails(session)
        resetMeasurementsView()
        bindMeasurements(session)
        stretchTableLayout(session)
    }

    override fun resetMeasurementsView() {
        super.resetMeasurementsView()
        mMeasurementValues.removeAllViews()
    }

    override fun bindMeasurements(session: Session) {
        session.streams.sortedBy { it.detailedType }.forEach { stream ->
            bindStream(stream.detailedType)
            bindLastMeasurement(stream)
        }
    }

    private fun bindLastMeasurement(stream: MeasurementStream) {
        val measurement = stream.measurements.lastOrNull()
        val valueView = mLayoutInflater.inflate(R.layout.measurement_value, null, false)


        val circleView = valueView.findViewById<ImageView>(R.id.circle_indicator)
        val valueTextView = valueView.findViewById<TextView>(R.id.measurement_value)

        if (measurement == null) {
            circleView.visibility = View.GONE
        } else {
            valueTextView.text = measurement.valueString()
            val level = measurement.getLevel(stream)
            if (level == null) {
                circleView.visibility = View.GONE
            } else {
                val color = MeasurementColor.get(context, level)
                circleView.setColorFilter(color)

                if (stream == mSelectedStream) {
                    valueView.background = SelectedSensorBorder(color)
                }
            }
        }

        mMeasurementValues.addView(valueView)
    }
}
