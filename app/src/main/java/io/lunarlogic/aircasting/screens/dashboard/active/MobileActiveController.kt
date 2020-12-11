package io.lunarlogic.aircasting.screens.dashboard.active

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.events.DisconnectExternalSensorsEvent
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.exceptions.BLENotSupported
import io.lunarlogic.aircasting.lib.NavigationController
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.observers.ActiveSessionsObserver
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.screens.dashboard.DashboardPagerAdapter
import io.lunarlogic.aircasting.screens.dashboard.SessionsController
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.AirBeamConnector
import io.lunarlogic.aircasting.sensor.AirBeamConnectorFactory
import org.greenrobot.eventbus.EventBus

class MobileActiveController(
    mRootActivity: FragmentActivity?,
    mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    mSettings: Settings,
    private val airBeamConnectorFactory: AirBeamConnectorFactory
): SessionsController(mRootActivity, mViewMvc, mSessionsViewModel, mSettings),
    SessionsViewMvc.Listener, AirBeamConnector.Listener {

    private var mSessionsObserver = ActiveSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)
    private var mDeviceIdToReconnect: String? = null
    private var mAirBeamConnector: AirBeamConnector? = null

    override fun registerSessionsObserver() {
        mSessionsObserver.observe(mSessionsViewModel.loadMobileActiveSessionsWithMeasurements())
    }

    override fun unregisterSessionsObserver() {
        mSessionsObserver.stop()
    }

    override fun onRecordNewSessionClicked() {
        startNewSession(Session.Type.MOBILE)
    }

    override fun onStopSessionClicked(sessionUUID: String) {
        val event = StopRecordingEvent(sessionUUID)
        EventBus.getDefault().post(event)

        val tabId = DashboardPagerAdapter.tabIndexForSessionType(
            Session.Type.MOBILE,
            Session.Status.FINISHED
        )
        NavigationController.goToDashboard(tabId)
    }

    override fun onDeleteSessionClicked(sessionUUID: String) {
        // do nothing
    }

    override fun onExpandSessionCard(session: Session) {
        // do nothing
    }

    override fun onDisconnectSessionClicked(sessionUUID: String) {
        disconnect()
    }

    override fun onReconnectSessionClicked(deviceId: String) {
        // disconnecting first to make sure the connector thread is stopped correctly etc
        disconnect()

        println("ANIA RECONNECT $deviceId")
        mDeviceIdToReconnect = deviceId

        val bm = BluetoothManager()
        // TODO: lookup in pairedDevices (for AB1 and AB2) if in paired devices that awesome
        // but otherwise start discovery again
        registerBluetoothDeviceFoundReceiver()
        bm.startDiscovery()
    }

    // TODO: move following methods to separate class
    override fun onBluetoothDeviceFound(deviceItem: DeviceItem) {
        if (deviceItem.id == mDeviceIdToReconnect) {
            unRegisterBluetoothDeviceFoundReceiver()
            reconnect(deviceItem)
        }
    }

    private fun disconnect() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
    }

    private fun reconnect(deviceItem: DeviceItem) {
        println("ANIA reconnecting to " + deviceItem.displayName() + "...")
        mAirBeamConnector = airBeamConnectorFactory.get(deviceItem)
        mAirBeamConnector?.registerListener(this)
        try {
            mAirBeamConnector?.connect(deviceItem)
        } catch (e: BLENotSupported) {
            mErrorHandler.handleAndDisplay(e)
        }
    }

    override fun onConnectionSuccessful(deviceId: String) {
        println("ANIA RECONNECTED!")
        mAirBeamConnector?.reconnectMobileSession()
    }
}
