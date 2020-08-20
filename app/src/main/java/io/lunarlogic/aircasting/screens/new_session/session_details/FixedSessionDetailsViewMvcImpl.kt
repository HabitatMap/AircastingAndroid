package io.lunarlogic.aircasting.screens.new_session.session_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import com.google.android.material.button.MaterialButtonToggleGroup
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.Session
import io.lunarlogic.aircasting.sensor.TAGS_SEPARATOR


class FixedSessionDetailsViewMvcImpl : BaseObservableViewMvc<SessionDetailsViewMvc.Listener>, SessionDetailsViewMvc {
    private var deviceId: String
    private var indoor = true
    private var streamingMethod = Session.StreamingMethod.CELLULAR

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        deviceId: String
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_fixed_session_details, parent, false)
        this.deviceId = deviceId

        val continueButton = rootView?.findViewById<Button>(R.id.continue_button)
        continueButton?.setOnClickListener {
            onSessionDetailsContinueClicked()
        }

        val indoorToggle = rootView?.findViewById<MaterialButtonToggleGroup>(R.id.indoor_toggle)
        indoorToggle?.addOnButtonCheckedListener { _, checkedId, isChecked ->
            indoor = (checkedId == R.id.indoor_button && isChecked)
        }

        val wifiCredentialsSection = rootView?.findViewById<ViewGroup>(R.id.wifi_credentials)

        val streamingMethofToggle = rootView?.findViewById<MaterialButtonToggleGroup>(R.id.streaming_method_toggle)
        streamingMethofToggle?.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (checkedId == R.id.cellular_button && isChecked) {
                streamingMethod = Session.StreamingMethod.CELLULAR
                wifiCredentialsSection?.visibility = View.INVISIBLE
            } else {
                streamingMethod = Session.StreamingMethod.WIFI
                wifiCredentialsSection?.visibility = View.VISIBLE
            }
        }
    }

    private fun onSessionDetailsContinueClicked() {
        val sessionName = getTextInputEditTextValue(R.id.session_name_input)
        val sessionTags = getSessionTags()
        val wifiName = getEditTextValue(R.id.wifi_name)
        val wifiPassword = getEditTextValue(R.id.wifi_password)

        val errorMessage = validate(sessionName, wifiName, wifiPassword)

        if (errorMessage == null) {
            notifyAboutSuccess(deviceId, sessionName, sessionTags, wifiName, wifiPassword)
        } else {
            notifyAboutValidationError(errorMessage)
        }
    }

    private fun notifyAboutValidationError(errorMessage: String) {
        for (listener in listeners) {
            listener.validationFailed(errorMessage)
        }
    }

    private fun notifyAboutSuccess(
        deviceId: String,
        sessionName: String,
        sessionTags: ArrayList<String>,
        wifiName: String,
        wifiPassword: String
    ) {
        for (listener in listeners) {
            listener.onSessionDetailsContinueClicked(
                deviceId,
                Session.Type.FIXED,
                sessionName,
                sessionTags,
                indoor,
                streamingMethod,
                wifiName,
                wifiPassword
            )
        }
    }

    private fun getSessionTags(): ArrayList<String> {
        val string = getTextInputEditTextValue(R.id.session_tags_input)
        return ArrayList(string.split(TAGS_SEPARATOR))
    }

    private fun validate(sessionName: String, wifiName: String, wifiPassword: String): String? {
        if (sessionName.isEmpty()) {
            return getString(R.string.session_name_required)
        }

        if (streamingMethod == Session.StreamingMethod.WIFI && (wifiName.isEmpty() || wifiPassword.isEmpty())) {
            return getString(R.string.session_wifi_credentials_required)
        }

        return null
    }
}
