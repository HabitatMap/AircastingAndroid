package io.lunarlogic.aircasting.screens.new_session.session_details

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.TAGS_SEPARATOR
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem

class MobileSessionDetailsViewMvcImpl : BaseObservableViewMvc<SessionDetailsViewMvc.Listener>, SessionDetailsViewMvc {
    private var sessionUUID: String
    private var deviceItem: DeviceItem

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        sessionUUID: String,
        deviceItem: DeviceItem
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_mobile_session_details, parent, false)
        this.sessionUUID = sessionUUID
        this.deviceItem = deviceItem

        val continueButton = rootView?.findViewById<Button>(R.id.continue_button)
        continueButton?.setOnClickListener {
            onSessionDetailsContinueClicked()
        }
    }

    private fun onSessionDetailsContinueClicked() {
        val sessionName = getTextInputEditTextValue(R.id.session_name_input) //TODO: validating if input is not empty
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
            listener.onSessionDetailsContinueClicked(sessionUUID, deviceItem, Session.Type.MOBILE, sessionName, sessionTags)
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
