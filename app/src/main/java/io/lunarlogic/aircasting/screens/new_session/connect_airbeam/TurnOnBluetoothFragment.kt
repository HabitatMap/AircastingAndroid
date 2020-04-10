package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class TurnOnBluetoothFragment(private val mListener: TurnOnBluetoothViewMvc.Listener) : Fragment() {
    private var mTurnOnBluetoothController: TurnOnBluetoothController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val turnOnBluetoothView =
            TurnOnBluetoothViewMvcImpl(
                layoutInflater,
                null
            )
        mTurnOnBluetoothController =
            TurnOnBluetoothController(
                context,
                turnOnBluetoothView
            )

        return turnOnBluetoothView.rootView
    }

    override fun onStart() {
        super.onStart()
        mTurnOnBluetoothController!!.registerListener(mListener)
    }

    override fun onStop() {
        super.onStop()
        mTurnOnBluetoothController!!.unregisterListener(mListener)
    }
}