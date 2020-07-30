package io.lunarlogic.aircasting.screens.new_session.select_session_type

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class SelectSessionTypeViewMvcImpl : BaseObservableViewMvc<SelectSessionTypeViewMvc.Listener>, SelectSessionTypeViewMvc {
    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.fragment_select_session_type, parent, false)

        val fixedSessionCard = rootView?.findViewById<CardView>(R.id.fixed_session_start_card)
        fixedSessionCard?.setOnClickListener {
            onFixedSessionSelected()
        }

        val mobileSessionCard = rootView?.findViewById<CardView>(R.id.mobile_session_start_card)
        mobileSessionCard?.setOnClickListener {
            onMobileSessionSelected()
        }
    }

    private fun onFixedSessionSelected() {
        for (listener in listeners) {
            listener.onFixedSessionSelected()
        }
    }

    private fun onMobileSessionSelected() {
        for (listener in listeners) {
            listener.onMobileSessionSelected()
        }
    }
}
