package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class DashboardViewMvcImpl : BaseObservableViewMvc<DashboardViewMvc.Listener>, DashboardViewMvc {
    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.fragment_dashboard, parent, false)

        val button = rootView?.findViewById<Button>(R.id.dashboard_record_new_session_button)
        button?.setOnClickListener {
            onRecordNewSessionClicked()
        }
    }

    fun onRecordNewSessionClicked() {
        for (listener in listeners) {
            listener.onRecordNewSessionClicked()
        }
    }
}