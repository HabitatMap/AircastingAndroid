package io.lunarlogic.aircasting.screens.dashboard.mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session

class MobileDormantSessionViewMvcImpl: BaseObservableViewMvc<MobileDormantSessionViewMvc.Listener>,
    MobileDormantSessionViewMvc {
    private val mLayoutInflater: LayoutInflater

    private var mDateTextView: TextView
    private var mNameTextView: TextView
    private var mTagsTextView: TextView
    private var mMeasurementsTable: TableLayout
    private var mMeasurementHeaders: TableRow
//    private var mDeleteSesssionButton: Button

    private var mSession: Session? = null

    constructor(inflater: LayoutInflater, parent: ViewGroup) {
        mLayoutInflater = inflater

        this.rootView = inflater.inflate(R.layout.dormant_session, parent, false)

        mDateTextView = findViewById(R.id.dormant_session_date)
        mNameTextView = findViewById(R.id.dormant_session_name)
        mTagsTextView = findViewById(R.id.dormant_session_tags)
        mMeasurementsTable = findViewById(R.id.measurements_table)
        mMeasurementHeaders = findViewById(R.id.measurement_headers)

//        mDeleteSesssionButton = findViewById(R.id.delete_session_button)
//
//        mDeleteSesssionButton.setOnClickListener(View.OnClickListener {
//            for (listener in listeners) {
//                listener.onSessionDeleteClicked(mSession!!)
//            }
//        })
    }

    override fun bindSession(session: Session) {
        mSession = session
        mDateTextView.setText(session.durationString())
        mNameTextView.setText(session.name)
        mTagsTextView.setText(session.tags.joinToString(", "))

        mMeasurementsTable.isStretchAllColumns = false
        mMeasurementHeaders.removeAllViews()
        session.streams.sortedBy { it.detailedType }.forEach { stream ->
            bindStream(stream.detailedType)
        }

        if (session.streams.size > 1) {
            mMeasurementsTable.isStretchAllColumns = true
        }
    }

    private fun bindStream(detailedType: String?) {
        val headerView = mLayoutInflater.inflate(R.layout.measurement_header, null, false)

        val headerTextView = headerView.findViewById<TextView>(R.id.measurement_header)
        headerTextView.text = detailedType

        mMeasurementHeaders.addView(headerView)
    }
}
