package io.lunarlogic.aircasting.screens.lets_start

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class LetsStartViewMvcImpl: BaseObservableViewMvc<LetsStartViewMvc.Listener>, LetsStartViewMvc {
    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.fragment_lets_start, parent, false)

        val button = rootView?.findViewById<Button>(R.id.start_recording_button)
        button?.setOnClickListener {
            onRecordNewSessionClicked()
        }
    }

    private fun onRecordNewSessionClicked() {
        for (listener in listeners) {
            listener.onRecordNewSessionClicked()
        }
    }
}