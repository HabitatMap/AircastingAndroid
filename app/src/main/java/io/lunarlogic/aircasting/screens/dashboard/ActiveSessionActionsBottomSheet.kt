package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BottomSheet
import kotlinx.android.synthetic.main.active_session_actions.view.*

class ActiveSessionActionsBottomSheet(private val mListener: Listener): BottomSheet(mListener) {
    interface Listener: BottomSheet.Listener {
        fun disconnectSessionPressed()
        fun reconnectSessionPressed()
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

        val disconnectButton = view?.disconnect_session_button
        disconnectButton?.setOnClickListener {
            mListener.disconnectSessionPressed()
        }

        val reconnectButton = view?.reconnect_session_button
        reconnectButton?.setOnClickListener {
            mListener.reconnectSessionPressed()
        }

        val stopButton = view?.stop_session_button
        stopButton?.setOnClickListener {
            mListener.stopSessionPressed()
        }

        return view
    }
}
