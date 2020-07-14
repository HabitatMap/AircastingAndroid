package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.sensor.Session
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class MobileSessionConfirmationViewMvcImpl: BaseObservableViewMvc<ConfirmationViewMvc.Listener>, ConfirmationViewMvc {
    private var session: Session? = null

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?,
        session: Session
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_mobile_session_confirmation, parent, false)
        this.session = session

        val sessionDescription = rootView?.findViewById<TextView>(R.id.description)
        val sessionDescriptionTemplate = inflater.context.getString(R.string.mobile_session_confirmation_description)
        sessionDescription?.text = sessionDescriptionTemplate.format(session.name)

        val startRecordingButton = rootView?.findViewById<Button>(R.id.start_recording_button)
        startRecordingButton?.setOnClickListener {
            onStartRecordingClicked()
        }
    }

    private fun onStartRecordingClicked() {
        for (listener in listeners) {
            listener.onStartRecordingClicked(session!!)
        }
    }
}