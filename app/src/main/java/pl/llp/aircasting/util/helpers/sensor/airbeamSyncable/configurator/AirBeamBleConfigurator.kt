package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.ConnectRequest
import no.nordicsemi.android.ble.observer.ConnectionObserver
import pl.llp.aircasting.data.api.services.FixedSessionConfig
import pl.llp.aircasting.data.model.Session

interface AirBeamBleConfigurator {
    fun setObserver(observer: ConnectionObserver)
    fun connectDevice(device: BluetoothDevice): ConnectRequest
    fun closeConnection()
    fun sendAuth(uuid: String)
    fun configure(session: Session, wifiSSID: String?, wifiPassword: String?, fixedSessionConfig: FixedSessionConfig? = null)
    fun reconnectMobileSession()
    fun triggerSDCardDownload()
    suspend fun clearSDCard()
    fun discardSession() {}
    fun reset()
    fun log(priority: Int, message: String)
}
