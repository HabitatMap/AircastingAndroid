package pl.llp.aircasting.ui.view.screens.new_session.session_details

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import com.google.android.material.textfield.TextInputLayout
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.TAGS_SEPARATOR
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem

class MobileSessionDetailsViewMvcImpl : BaseObservableViewMvc<SessionDetailsViewMvc.Listener>, SessionDetailsViewMvc {
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

        val intervals = listOf(
            context.getString(R.string.session_interval_10min),
            context.getString(R.string.session_interval_5min),
            context.getString(R.string.session_interval_1min),
            context.getString(R.string.session_interval_5s),
            context.getString(R.string.session_interval_1s)
        )
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, intervals)
        val sessionIntervalInput = rootView?.findViewById<AutoCompleteTextView>(R.id.session_interval_input)
        sessionIntervalInput?.setAdapter(adapter)
        sessionIntervalInput?.setText(context.getString(R.string.session_interval_1s), false)
    }

    private fun onSessionDetailsContinueClicked() {
        val sessionName = getTextInputEditTextValue(R.id.session_name_input)
        val sessionTags = getSessionTags()
        val selectedIntervalText = rootView?.findViewById<AutoCompleteTextView>(R.id.session_interval_input)?.text?.toString()
        val intervalSeconds = when (selectedIntervalText) {
            context.getString(R.string.session_interval_10min) -> 600
            context.getString(R.string.session_interval_5min) -> 300
            context.getString(R.string.session_interval_1min) -> 60
            context.getString(R.string.session_interval_5s) -> 5
            context.getString(R.string.session_interval_1s) -> 1
            else -> 1
        }

        val errorMessage = validate(sessionName, intervalSeconds)

        if (errorMessage == null) {
            notifyAboutSuccess(sessionName, sessionTags, intervalSeconds)
        } else {
            notifyAboutValidationError(errorMessage)
        }
    }

    private fun notifyAboutValidationError(errorMessage: String) {
        for (listener in listeners) {
            listener.validationFailed(errorMessage)
        }
    }

    private fun notifyAboutSuccess(sessionName: String, sessionTags: ArrayList<String>, intervalSeconds: Int) {
        for (listener in listeners) {
            listener.onSessionDetailsContinueClicked(
                sessionUUID,
                deviceItem,
                Session.Type.MOBILE,
                sessionName,
                sessionTags,
                intervalSeconds = intervalSeconds,
            )
        }
    }

    private fun getSessionTags(): ArrayList<String> {
        val string = getTextInputEditTextValue(R.id.session_tags_input)
        return ArrayList(string.split(TAGS_SEPARATOR))
    }

    private fun validate(sessionName: String, intervalSeconds: Int?): String? {
        if (sessionName.isEmpty()) {
            sessionNameInputLayout?.error = " "
            return getString(R.string.session_name_required)
        }
        if (intervalSeconds == null || intervalSeconds <= 0) {
            return getString(R.string.session_interval_required)
        }

        return null
    }
}
