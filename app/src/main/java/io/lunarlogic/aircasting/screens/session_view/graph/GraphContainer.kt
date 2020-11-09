package io.lunarlogic.aircasting.screens.session_view.graph

import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc

class GraphContainer {
    private val mContext: Context
    private var mListener: SessionDetailsViewMvc.Listener? = null

    private var mSessionPresenter: SessionPresenter? = null
    private var mMeasurements: List<Measurement> = emptyList()

    constructor(rootView: View?, context: Context, supportFragmentManager: FragmentManager?) {
        mContext = context
    }

    fun registerListener(listener: SessionDetailsViewMvc.Listener) {
        mListener = listener
    }

    fun unregisterListener() {
        mListener = null
    }


    fun bindSession(sessionPresenter: SessionPresenter?) {
        mSessionPresenter = sessionPresenter

    }

    fun addMeasurement(measurement: Measurement) {

    }

    fun refresh(sessionPresenter: SessionPresenter?) {
        bindSession(sessionPresenter)
    }
}
