package io.lunarlogic.aircasting.screens.dashboard.fixed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.Session

class FixedSessionViewMvcImpl: BaseObservableViewMvc<FixedSessionViewMvc.Listener>,
    FixedSessionViewMvc {
    private var mDateTextView: TextView
    private var mNameTextView: TextView
    private var mTagsTextView: TextView
    private var mMeasurementsTextView: TextView
//    private var mDeleteSesssionButton: Button

    private var mSession: Session? = null

    constructor(inflater: LayoutInflater, parent: ViewGroup) {
        this.rootView = inflater.inflate(R.layout.dormant_session, parent, false)

        mDateTextView = findViewById(R.id.dormant_session_date)
        mNameTextView = findViewById(R.id.dormant_session_name)
        mTagsTextView = findViewById(R.id.dormant_session_tags)
        mMeasurementsTextView = findViewById(R.id.session_measurements)
//        mDeleteSesssionButton = findViewById(R.id.delete_session_button)

//        mDeleteSesssionButton.setOnClickListener(View.OnClickListener {
//            for (listener in listeners) {
//                listener.onSessionDeleteClicked(mSession!!)
//            }
//        })
    }

    override fun bindSession(session: Session) {
        mSession = session
        mDateTextView.setText(session.startTime.toString())
        mNameTextView.setText(session.name)
        mTagsTextView.setText(session.tags.joinToString(", "))

        // TODO: handle
        val measurementsString = session.streams.map { stream ->
            stream.detailedType
        }.joinToString("\n")
        mMeasurementsTextView.setText(measurementsString)
    }
}
