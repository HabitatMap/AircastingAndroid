package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.screens.new_session.NewSessionWizardNavigator
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Connector
import io.lunarlogic.aircasting.sensor.Session

class ConnectingAirBeamFragment() : Fragment(), NewSessionWizardNavigator.BackPressedListener {
    lateinit private var controller: ConnectingAirBeamController
    lateinit var deviceItem: DeviceItem
    lateinit var sessionType: Session.Type
    lateinit var listener: ConnectingAirBeamController.Listener
    lateinit var airbeam2Connector: AirBeam2Connector

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            ConnectingAirBeamViewMvcImpl(
                layoutInflater,
                null
            )

        controller = ConnectingAirBeamController(context!!, deviceItem, sessionType, airbeam2Connector, listener)

        return view.rootView
    }

    override fun onStart() {
        super.onStart()
        controller.onStart()
    }

    override fun onBackPressed() {
        controller.onBackPressed()
    }
}