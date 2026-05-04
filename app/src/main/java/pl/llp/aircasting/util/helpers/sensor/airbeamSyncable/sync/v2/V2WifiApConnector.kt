package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import pl.llp.aircasting.data.api.util.TAG

/**
 * Drives the user through joining the V2 firmware's "AirBeamMini Sync" SoftAP, then exposes
 * the chosen [Network] so [V2SyncFileDownloader] can route its `GET /sync` over the AP.
 *
 * Why a manual flow rather than [android.net.wifi.WifiNetworkSpecifier]: in practice the
 * system Wi-Fi picker driven by `requestNetwork(specifier)` is unreliable across OEM
 * firmwares and Android versions — the picker either shows an empty list, refuses to
 * display the freshly-started SoftAP, or times out without giving the user a chance to tap
 * "Connect" (observed on Android 12). The polling approach below works on every Android
 * version we target: the user lands in the system Wi-Fi settings with the password on
 * screen, joins normally, and the app resumes once it sees the device attach to the AP.
 */
class V2WifiApConnector(
    private val applicationContext: Context,
    private val ssid: String = AP_SSID,
) {
    companion object {
        const val AP_SSID = "AirBeamMini Sync"
        private const val JOIN_TIMEOUT_MS = 120_000L
        private const val POLL_INTERVAL_MS = 500L
        private const val BIND_TIMEOUT_MS = 6_000L
    }

    private val connectivityManager: ConnectivityManager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val wifiManager: WifiManager =
        applicationContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private var trackingCallback: ConnectivityManager.NetworkCallback? = null

    /**
     * Show the user the password, open system Wi-Fi settings, and poll until the device is
     * associated with [ssid]. Runs [block] with the bound [Network] in scope; on completion
     * the process-wide binding is released.
     *
     * Returns whatever [block] returns, or null if the AP could not be joined within
     * [JOIN_TIMEOUT_MS] (timeout / user cancellation).
     */
    suspend fun <T> withApConnection(password: String, block: suspend (Network?) -> T): T? {
        showPasswordToast(password)
        openWifiSettings()

        val network = waitForApAssociation()
        if (network == null) {
            Log.e(TAG, "V2WifiAp: user did not connect to '$ssid' within ${JOIN_TIMEOUT_MS / 1000}s")
            disconnect()
            return null
        }

        return try {
            block(network)
        } finally {
            disconnect()
        }
    }

    private fun showPasswordToast(password: String) {
        val msg = "Connect to '$ssid' (password $password) to sync measurements"
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
        } else {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
            }
        }
        Log.d(TAG, "V2WifiAp: prompting user to join '$ssid' (passwordLen=${password.length})")
    }

    private fun openWifiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { applicationContext.startActivity(intent) }
            .onFailure { Log.w(TAG, "V2WifiAp: could not open Wi-Fi settings: ${it.message}") }
    }

    @SuppressLint("MissingPermission")
    private suspend fun waitForApAssociation(): Network? {
        val deadline = System.currentTimeMillis() + JOIN_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            val info = wifiManager.connectionInfo
            val currentSsid = info?.ssid?.trim('"')
            if (currentSsid == ssid) {
                Log.d(TAG, "V2WifiAp: device associated with '$ssid'")
                return bindToWifiNetwork()
            }
            delay(POLL_INTERVAL_MS)
        }
        return null
    }

    /**
     * Find the currently active Wi-Fi [Network] and pin it to this process so OkHttp routes
     * through it. Registers a [NetworkRequest] with TRANSPORT_WIFI; the already-connected
     * Wi-Fi network triggers `onAvailable` synchronously.
     */
    private suspend fun bindToWifiNetwork(): Network? = withContext(Dispatchers.IO) {
        val deferred = CompletableDeferred<Network?>()
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "V2WifiAp: bound Wi-Fi network: $network")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connectivityManager.bindProcessToNetwork(network)
                }
                if (!deferred.isCompleted) deferred.complete(network)
            }

            override fun onUnavailable() {
                Log.e(TAG, "V2WifiAp: Wi-Fi network unavailable in bind step")
                if (!deferred.isCompleted) deferred.complete(null)
            }
        }
        trackingCallback = callback
        connectivityManager.requestNetwork(request, callback, BIND_TIMEOUT_MS.toInt())
        withTimeoutOrNull(BIND_TIMEOUT_MS + 1_000L) { deferred.await() }
    }

    private fun disconnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.bindProcessToNetwork(null)
        }
        trackingCallback?.let {
            runCatching { connectivityManager.unregisterNetworkCallback(it) }
            trackingCallback = null
        }
    }
}
