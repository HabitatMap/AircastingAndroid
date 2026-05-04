package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import pl.llp.aircasting.data.api.util.TAG

/**
 * Drives the V2 manual-sync SoftAP join via `WifiNetworkSpecifier` + the system Wi-Fi
 * picker. Picker pre-fills the SSID + password we received over BLE, so the user only
 * confirms — they never have to type credentials or risk picking the wrong network.
 *
 * Critical: on Android 13+ (API 33) the picker requires the `NEARBY_WIFI_DEVICES`
 * runtime permission. Without it the picker silently shows an empty list, even when the
 * SSID is otherwise visible in regular Wi-Fi scans. The orchestrator must request the
 * permission before invoking [withApConnection].
 *
 * Once the user taps "Connect", the system attaches a transient app-scoped [Network] and
 * fires [ConnectivityManager.NetworkCallback.onAvailable]. We bind the process to that
 * Network so OkHttp routes the GET through the SoftAP rather than the user's home Wi-Fi.
 * The binding is released in [disconnect] (called from the `finally` block of
 * [withApConnection]), at which point the system tears the AP connection down and the
 * device returns to its previous network automatically.
 */
class V2WifiApConnector(
    private val applicationContext: Context,
    private val ssid: String = AP_SSID,
) {
    companion object {
        const val AP_SSID = "AirBeamMini Sync"
        // Generous timeout: covers SoftAP beacon propagation + system picker scan + the
        // user noticing and tapping "Connect".
        private const val CONNECT_TIMEOUT_MS = 90_000L
        // Brief pause before requesting the network so the SoftAP beacons have time to be
        // picked up by the next Wi-Fi scan; otherwise the picker can open empty.
        private const val PRE_REQUEST_DELAY_MS = 2_000L
    }

    private val connectivityManager: ConnectivityManager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val wifiManager: WifiManager =
        applicationContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private var modernCallback: ConnectivityManager.NetworkCallback? = null

    /**
     * Connect to the AP and run [block] with the bound [Network] available; on completion
     * (success or exception) restore the previous binding.
     *
     * Returns whatever [block] returns, or null if the AP could not be joined within
     * [CONNECT_TIMEOUT_MS] (timeout, user cancellation, or `NEARBY_WIFI_DEVICES` missing).
     */
    suspend fun <T> withApConnection(password: String, block: suspend (Network) -> T): T? {
        val network = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectModern(password)
        } else {
            Log.e(TAG, "V2WifiAp: API < 29 not supported for V2 manual sync")
            null
        }

        if (network == null) {
            Log.e(TAG, "V2WifiAp: AP join failed — skipping HTTP step")
            disconnect()
            return null
        }

        return try {
            block(network)
        } finally {
            disconnect()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun connectModern(password: String): Network? {
        Log.d(TAG, "V2WifiAp: requesting SoftAP network ssid='$ssid' (passwordLen=${password.length})")
        // Trigger an explicit scan so the system Wi-Fi picker has fresh results that include
        // the just-started SoftAP. startScan() is deprecated/rate-limited on modern Android
        // but is still the best signal we can give the framework here; failure is non-fatal.
        runCatching { @Suppress("DEPRECATION") wifiManager.startScan() }
            .onFailure { Log.w(TAG, "V2WifiAp: startScan failed (best-effort): ${it.message}") }
        delay(PRE_REQUEST_DELAY_MS)

        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()

        val deferred = CompletableDeferred<Network?>()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "V2WifiAp: SoftAP network available: $network")
                connectivityManager.bindProcessToNetwork(network)
                if (!deferred.isCompleted) deferred.complete(network)
            }

            override fun onUnavailable() {
                Log.e(TAG, "V2WifiAp: SoftAP network unavailable (picker dismissed / no match / user denied)")
                if (!deferred.isCompleted) deferred.complete(null)
            }

            override fun onLost(network: Network) {
                Log.w(TAG, "V2WifiAp: SoftAP network lost: $network")
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                Log.d(TAG, "V2WifiAp: capabilities changed on $network: $caps")
            }
        }
        modernCallback = callback
        connectivityManager.requestNetwork(request, callback, CONNECT_TIMEOUT_MS.toInt())

        return withTimeoutOrNull(CONNECT_TIMEOUT_MS) { deferred.await() }
    }

    private fun disconnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.bindProcessToNetwork(null)
        }
        modernCallback?.let {
            runCatching { connectivityManager.unregisterNetworkCallback(it) }
            modernCallback = null
        }
    }
}
