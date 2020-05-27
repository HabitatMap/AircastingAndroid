package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.TAGS_SEPARATOR

class SessionDetailsViewMvcImpl : BaseObservableViewMvc<SessionDetailsViewMvc.Listener>, SessionDetailsViewMvc {
    private var deviceId: String

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        deviceId: String
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_session_details, parent, false)
        this.deviceId = deviceId

        val blueToothDeviceButton = rootView?.findViewById<Button>(R.id.continue_button)
        blueToothDeviceButton?.setOnClickListener {
            onSessionDetailsContinueClicked()
        }
    }

    private fun onSessionDetailsContinueClicked() {
        val sessionName = getSessionName()
        val sessionTags = getSessionTags()

        if (sessionName.isEmpty()) {
            notifyAboutValidationError()
        } else {
            notifyAboutSuccess(deviceId, sessionName, sessionTags)
        }
    }

    private fun notifyAboutValidationError() {
        for (listener in listeners) {
            listener.validationFailed()
        }
    }

    private fun notifyAboutSuccess(deviceId: String, sessionName: String, sessionTags: ArrayList<String>) {
        for (listener in listeners) {
            listener.onSessionDetailsContinueClicked(deviceId, sessionName, sessionTags)
        }
    }

    private fun getSessionName(): String {
        val sessionNameField = rootView?.findViewById<EditText>(R.id.session_name)
        return sessionNameField?.text.toString()
    }

    private fun getSessionTags(): ArrayList<String> {
        val sessionTagsField = rootView?.findViewById<EditText>(R.id.session_tags)
        return ArrayList(sessionTagsField?.text.toString().split(TAGS_SEPARATOR))
    }
}