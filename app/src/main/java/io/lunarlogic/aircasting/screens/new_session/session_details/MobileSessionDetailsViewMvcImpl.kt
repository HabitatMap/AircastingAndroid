package io.lunarlogic.aircasting.screens.new_session.session_details

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.ValidationHelper
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.TAGS_SEPARATOR
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem

class MobileSessionDetailsViewMvcImpl : SessionDetailsViewMvcImpl, SessionDetailsViewMvc {
    private var sessionUUID: String
    private var deviceItem: DeviceItem

    private var sessionNameInputLayout: TextInputLayout? = null

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        sessionUUID: String,
        deviceItem: DeviceItem
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_mobile_session_details, parent, false)
        this.sessionUUID = sessionUUID
        this.deviceItem = deviceItem

        sessionNameInputLayout = rootView?.findViewById<TextInputLayout>(R.id.session_name)

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
            listener.onSessionDetailsContinueClicked(sessionUUID, deviceItem, Session.Type.MOBILE, sessionName, sessionTags)
        }
    }

    private fun getSessionTags(): ArrayList<String> {
        val string = getTextInputEditTextValue(R.id.session_tags_input)
        return ArrayList(string.split(TAGS_SEPARATOR))
    }

    private fun validate(sessionName: String): String? {
        if (sessionName.isEmpty()) {
            sessionNameInputLayout?.error = " "
            return getString(R.string.session_name_required)
        }

        return null
    }
}
