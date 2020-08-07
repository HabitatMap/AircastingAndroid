package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BottomSheet

class ActiveSessionActionsBottomSheet(private val mListener: Listener): BottomSheet(mListener) {
    interface Listener: BottomSheet.Listener {
        fun stopSessionPressed()
    }

    override fun layoutId(): Int {
        return R.layout.active_session_actions;
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val stopButton = view?.findViewById<Button>(R.id.stop_session_button)
        stopButton?.setOnClickListener {
            mListener.stopSessionPressed()
        }

        return view
    }
}
