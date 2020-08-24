package io.lunarlogic.aircasting.screens.new_session.session_details

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager

class FixedSessionDetailsController(
    private val mContext: Context?,
    private val mViewMvc: FixedSessionDetailsViewMvc
): SessionDetailsController(mContext, mViewMvc), FixedSessionDetailsViewMvc.OnRefreshNetworksListener {

    private var mWifiManager: WifiManager? = null

    inner class WifiReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent?.action)) {
                val wifiList = mWifiManager?.getScanResults()
                wifiList?.let {
                    val networkList = wifiList
                        .filter { !it.SSID.isEmpty() }
                        .map { Network(it.SSID, it.level) }
                    mViewMvc.bindNetworks(networkList)
                }
            }

            mContext?.unregisterReceiver(this)
        }

    }

    override fun onCreate() {
        super.onCreate()

        mWifiManager = mContext?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        scanForNetworks()

        mViewMvc.registerOnRefreshNetworksListener(this)
    }

    override fun onRefreshClicked() {
        scanForNetworks()
    }

    private fun scanForNetworks() {
        registerNetworksReceiver()
        mWifiManager?.startScan()
    }

    private fun registerNetworksReceiver() {
        val wifiReceiver = WifiReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        mContext?.registerReceiver(wifiReceiver, intentFilter)
    }
}
