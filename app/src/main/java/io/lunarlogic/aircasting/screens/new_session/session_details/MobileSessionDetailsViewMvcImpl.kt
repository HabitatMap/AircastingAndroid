package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.Session
import io.lunarlogic.aircasting.sensor.TAGS_SEPARATOR

class MobileSessionDetailsViewMvcImpl : BaseObservableViewMvc<SessionDetailsViewMvc.Listener>, SessionDetailsViewMvc {
    private var deviceId: String

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        deviceId: String
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_mobile_session_details, parent, false)
        this.deviceId = deviceId

        val continueButton = rootView?.findViewById<Button>(R.id.continue_button)
        continueButton?.setOnClickListener {
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
            listener.onSessionDetailsContinueClicked(deviceId, Session.Type.MOBILE, sessionName, sessionTags)
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