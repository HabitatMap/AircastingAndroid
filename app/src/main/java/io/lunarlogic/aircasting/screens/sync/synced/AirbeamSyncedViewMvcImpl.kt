package io.lunarlogic.aircasting.screens.sync.synced

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class AirbeamSyncedViewMvcImpl: BaseObservableViewMvc<AirbeamSyncedViewMvc.Listener>, AirbeamSyncedViewMvc {

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_airbeam_synced, parent, false)

        val button = rootView?.findViewById<Button>(R.id.airbeam_synced_continue_button)
        button?.setOnClickListener {
            onAirbeamSyncedContinueClicked()
        }
    }

    private fun onAirbeamSyncedContinueClicked() {
        for (listener in listeners) {
            listener.onAirbeamSyncedContinueClicked()
        }
    }
}
