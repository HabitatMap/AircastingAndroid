package io.lunarlogic.aircasting.screens.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.MeasurementColor
import io.lunarlogic.aircasting.screens.common.BaseViewMvc
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session
import kotlinx.android.synthetic.main.activity_map.view.*

class MapViewMvcImpl: BaseViewMvc, MapViewMvc {
    private val mLayoutInflater: LayoutInflater

    private val mSessionDateTextView: TextView?
    private val mSessionNameTextView: TextView?
    private val mSessionTagsTextView: TextView?

    private val mSessionMeasurementsTable: TableLayout?
    private val mSessionMeasurementHeaders: TableRow?
    private val mMeasurementValues: TableRow?

    private var mSession: Session? = null

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): super() {
        mLayoutInflater = inflater

        this.rootView = inflater.inflate(R.layout.activity_map, parent, false)

        mSessionDateTextView = this.rootView?.session_date
        mSessionNameTextView = this.rootView?.session_name
        mSessionTagsTextView = this.rootView?.session_tags

        mSessionMeasurementsTable = this.rootView?.measurements_table
        mSessionMeasurementHeaders = this.rootView?.measurement_headers
        mMeasurementValues = this.rootView?.measurement_values
    }

    override fun bindSession(session: Session) {
        bindSessionDetails(session)
        resetMeasurementsView()
        bindMeasurements(session)
        stretchTableLayout(session)
    }

    protected fun bindSessionDetails(session: Session) {
        mSession = session
        mSessionDateTextView?.text = session.durationString()
        mSessionNameTextView?.text = session.name
        mSessionTagsTextView?.text = session.tagsString()
    }

    private fun resetMeasurementsView() {
        mSessionMeasurementsTable?.isStretchAllColumns = false
        mSessionMeasurementHeaders?.removeAllViews()
    }

    private fun bindMeasurements(session: Session) {
        session.streams.sortedBy { it.detailedType }.forEach { stream ->
            bindStream(stream.detailedType)
            bindLastMeasurement(stream)
        }
    }

    private fun stretchTableLayout(session: Session) {
        if (session.streams.size > 1) {
            mSessionMeasurementsTable?.isStretchAllColumns = true
        }
    }

    private fun bindStream(detailedType: String?) {
        val headerView = mLayoutInflater.inflate(R.layout.measurement_header, null, false)

        val headerTextView = headerView.findViewById<TextView>(R.id.measurement_header)
        headerTextView.text = detailedType

        mSessionMeasurementHeaders?.addView(headerView)
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
                circleView.setColorFilter(MeasurementColor.get(context, level))
            }
        }

        mMeasurementValues?.addView(valueView)
    }
}
