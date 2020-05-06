package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class SessionDetailsViewMvcImpl : BaseObservableViewMvc<SessionDetailsViewMvc.Listener>, SessionDetailsViewMvc {
    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.fragment_session_details, parent, false)

        val blueToothDeviceButton = rootView?.findViewById<Button>(R.id.continue_button)
        blueToothDeviceButton?.setOnClickListener {
            onSessionDetailsContinueClicked()
        }
    }

    private fun onSessionDetailsContinueClicked() {
        val sessionName = getSessionName()
        val sessionTags = getSessionTags()

        for (listener in listeners) {
            listener.onSessionDetailsContinueClicked(sessionName, sessionTags)
        }
    }

    private fun getSessionName(): String {
        val sessionNameField = rootView?.findViewById<EditText>(R.id.session_name)
        return sessionNameField?.text.toString()
    }

    private fun getSessionTags(): List<String> {
        val sessionTagsField = rootView?.findViewById<EditText>(R.id.session_tags)
        return sessionTagsField?.text.toString().split(" ")
    }
}