package io.lunarlogic.aircasting.screens.new_session.session_details

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.Session
import io.lunarlogic.aircasting.sensor.TAGS_SEPARATOR

class MobileSessionDetailsViewMvcImpl : BaseObservableViewMvc<SessionDetailsViewMvc.Listener>, SessionDetailsViewMvc {
    private var sessionUUID: String
    private var deviceId: String

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        sessionUUID: String,
        deviceId: String
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_mobile_session_details, parent, false)
        this.sessionUUID = sessionUUID
        this.deviceId = deviceId

        val continueButton = rootView?.findViewById<Button>(R.id.continue_button)
        continueButton?.setOnClickListener {
            onSessionDetailsContinueClicked()
        }
    }

    private fun onSessionDetailsContinueClicked() {
        val sessionName = getTextInputEditTextValue(R.id.session_name_input)
        val sessionTags = getSessionTags()

        val errorMessage = validate(sessionName)

        if (errorMessage == null) {
            notifyAboutSuccess(sessionName, sessionTags)
        } else {
            notifyAboutValidationError(errorMessage)
        }
    }

    private fun notifyAboutValidationError(errorMessage: String) {
        for (listener in listeners) {
            listener.validationFailed(errorMessage)
        }
    }

    private fun notifyAboutSuccess(sessionName: String, sessionTags: ArrayList<String>) {
        for (listener in listeners) {
            listener.onSessionDetailsContinueClicked(sessionUUID, deviceId, Session.Type.MOBILE, sessionName, sessionTags)
        }
    }

    private fun getSessionTags(): ArrayList<String> {
        val string = getTextInputEditTextValue(R.id.session_tags_input)
        return ArrayList(string.split(TAGS_SEPARATOR))
    }

    private fun validate(sessionName: String): String? {
        if (sessionName.isEmpty()) {
            return getString(R.string.session_name_required)
        }

        return null
    }
}
