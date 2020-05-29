package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.Session

class SessionViewMvcImpl: BaseObservableViewMvc<SessionViewMvc.Listener>,
    SessionViewMvc {
    private var mDateTextView: TextView
    private var mNameTextView: TextView
    private var mTagsTextView: TextView
    private var mMeasurementsTextView: TextView
    private var mStopSesssionButton: Button

    private var mSession: Session? = null

    constructor(inflater: LayoutInflater, parent: ViewGroup) {
        this.rootView = inflater.inflate(R.layout.session, parent, false)

        mDateTextView = findViewById(R.id.session_date)
        mNameTextView = findViewById(R.id.session_name)
        mTagsTextView = findViewById(R.id.session_tags)
        mMeasurementsTextView = findViewById(R.id.session_measurements)
        mStopSesssionButton = findViewById(R.id.stop_session_button)

        mStopSesssionButton?.setOnClickListener(View.OnClickListener {
            for (listener in listeners) {
                listener.onSessionStopClicked(mSession!!)
            }
        })
    }

    override fun bindSession(session: Session) {
        mSession = session
        mDateTextView.setText(session.startTime.toString())
        mNameTextView.setText(session.name)
        mTagsTextView.setText(session.tags.joinToString(", "))

        if (session.isRecording()) {
            showStopButton()
        } else {
            hideStopButton()
        }

        // TODO: handle
        val measurementsString = session.streams.map { stream ->
            val measurement: Measurement? = stream.measurements.last()
            "${stream.detailedType}: ${measurement?.value} ${stream.unitSymbol}"
        }.joinToString("\n")
        mMeasurementsTextView.setText(measurementsString)
    }

    private fun showStopButton() {
        mStopSesssionButton.visibility = View.VISIBLE
    }

    private fun hideStopButton() {
        mStopSesssionButton.visibility = View.INVISIBLE
    }
}