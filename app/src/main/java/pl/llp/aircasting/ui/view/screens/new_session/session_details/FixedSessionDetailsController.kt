package pl.llp.aircasting.ui.view.screens.new_session.session_details

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.NewSessionViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.TurnOnWifiDialog
import pl.llp.aircasting.util.isSDKGreaterOrEqualToQ

class FixedSessionDetailsController(
    private val mContextActivity: FragmentActivity?,
    private val mViewMvc: FixedSessionDetailsViewMvc?,
    private val mFragmentManager: FragmentManager
) : SessionDetailsController(mContextActivity, mViewMvc),
    FixedSessionDetailsViewMvc.OnStreamingMethodChangedListener,
    FixedSessionDetailsViewMvc.OnRefreshNetworksListener,
    FixedSessionDetailsViewMvc.OnNetworkDialogStateChangedListener,
    NewSessionViewMvc.TurnOnWifiDialogListener {

    private var mWifiManager: WifiManager? = null
    private var mWeDoNotLeakPasswordsBottomSheet: WeDoNotLeakPasswordsBottomSheet? = null
    private var needToShowInfoBottomSheet = true

    inner class WifiReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent?.action)) {
                val wifiList = mWifiManager?.scanResults
                wifiList?.let {
                    val networkList = wifiList
                        .map { Network(it.SSID, it.level, it.frequency) }
                        .filter { !it.name.isEmpty() }
                        .filter { it.frequency <= Network.MAX_FREQUENCY }
                        .distinct()
                    mViewMvc?.bindNetworks(networkList)
                }
            }

            mContextActivity?.unregisterReceiver(this)
        }

    }

    override fun onCreate() {
        super.onCreate()

        mWifiManager =
            mContextActivity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as? WifiManager

        mViewMvc?.registerOnStreamingMethodChangedListener(this)
        mViewMvc?.registerOnRefreshNetworksListener(this)
        mViewMvc?.registerOnNetworkDialogStateChangedListener(this)

        scanForNetworks()
    }

    override fun onPause() {
        if (needToShowInfoBottomSheet) {
            mWeDoNotLeakPasswordsBottomSheet =
                mWeDoNotLeakPasswordsBottomSheet ?: WeDoNotLeakPasswordsBottomSheet()

            mWeDoNotLeakPasswordsBottomSheet?.show(mFragmentManager)
        }
    }

    override fun onStop() {
        mWeDoNotLeakPasswordsBottomSheet?.dismissAllowingStateLoss()
    }

    override fun onResume() {
        mWeDoNotLeakPasswordsBottomSheet?.dismiss()
    }

    override fun onStreamingMethodChanged(streamingMethod: Session.StreamingMethod) {
        if (streamingMethod != Session.StreamingMethod.WIFI) return

        if (mWifiManager?.isWifiEnabled == true) {
            scanForNetworks()
        } else {
            val dialog = TurnOnWifiDialog(mFragmentManager, this)
            dialog.show()
        }
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
        mContextActivity?.registerReceiver(wifiReceiver, intentFilter)
    }

    override fun turnOnWifiClicked() {
        val startForResult =
            mContextActivity?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                scanForNetworks()
            }

        if (isSDKGreaterOrEqualToQ()) {
            val intent = Intent(Settings.Panel.ACTION_WIFI)
            startForResult?.launch(intent)
        } else {
            mWifiManager?.isWifiEnabled = true
            scanForNetworks()
        }
    }

    override fun onDialogIsAboutToBeShown() {
         needToShowInfoBottomSheet = false
    }

    override fun onDialogIsAboutToBeClosed() {
        needToShowInfoBottomSheet = true
    }
}
