package pl.llp.aircasting.util.helpers.sensor

import android.content.Context
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.airbeam2.AirBeam2Connector
import pl.llp.aircasting.util.helpers.sensor.airbeam3.AirBeam3Connector

open class AirBeamConnectorFactory(
    private val mContext: Context,
    private val mSettings: Settings,
    private val mErrorHandler: ErrorHandler,
    private val bluetoothManager: BluetoothManager
) {
    private val ab2connector by lazy {
        AirBeam2Connector(
            mSettings,
            mErrorHandler,
            bluetoothManager
        )
    }
    private val ab3connector by lazy {
        AirBeam3Connector(
            mContext,
            mSettings,
            mErrorHandler,
            bluetoothManager
        )
    }

    open fun get(deviceItem: DeviceItem): AirBeamConnector? {
        return when (deviceItem.type) {
            DeviceItem.Type.AIRBEAM3 -> ab3connector
            else -> ab2connector
        }
    }
}
