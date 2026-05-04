package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import pl.llp.aircasting.data.api.util.TAG

/**
 * Joins the V2 firmware's "AirBeam Mini Sync" SoftAP, downloads measurements via
 * [V2SyncFileDownloader], then restores the previous network binding.
 *
 * On API 29+ uses [WifiNetworkSpecifier] with [ConnectivityManager.requestNetwork] to obtain
 * a transient, app-scoped network without modifying the user's saved networks. The returned
 * [Network] is bound process-wide so OkHttp routes the GET through the AP rather than the
 * default mobile/Wi-Fi network.
 *
 * On API < 29 falls back to [WifiManager.addNetwork] + [WifiManager.enableNetwork], which
 * temporarily reconnects the entire device to the AP. Best-effort restore by re-enabling
 * the previously connected network.
 */
class V2WifiApConnector(
    private val applicationContext: Context,
    private val ssid: String = AP_SSID,
) {
    companion object {
        const val AP_SSID = "AirBeamMini Sync"
        private const val CONNECT_TIMEOUT_MS = 30_000L
    }

    private val connectivityManager: ConnectivityManager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val wifiManager: WifiManager =
        applicationContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private var modernCallback: ConnectivityManager.NetworkCallback? = null
    private var legacyAddedNetworkId: Int = -1
    private var legacyPreviousNetworkId: Int = -1

    /**
     * Connect to the AP and run [block] with the bound [Network] available; on completion
     * (success or exception) restore the previous binding.
     *
     * Returns whatever [block] returns, or null if the AP could not be joined within
     * [CONNECT_TIMEOUT_MS].
     */
    suspend fun <T> withApConnection(password: String, block: suspend (Network?) -> T): T? {
        val network: Network? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectModern(password)
        } else {
            connectLegacy(password)
            null
        }

        return try {
            block(network)
        } finally {
            disconnect()
        }
    }

    private suspend fun connectModern(password: String): Network? {
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
                Log.e(TAG, "V2WifiAp: SoftAP network unavailable")
                if (!deferred.isCompleted) deferred.complete(null)
            }

            override fun onLost(network: Network) {
                Log.w(TAG, "V2WifiAp: SoftAP network lost: $network")
            }
        }
        modernCallback = callback
        connectivityManager.requestNetwork(request, callback, CONNECT_TIMEOUT_MS.toInt())

        return withTimeoutOrNull(CONNECT_TIMEOUT_MS) { deferred.await() }
    }

    @Suppress("DEPRECATION")
    private suspend fun connectLegacy(password: String) {
        legacyPreviousNetworkId = wifiManager.connectionInfo?.networkId ?: -1

        val config = WifiConfiguration().apply {
            SSID = "\"$ssid\""
            preSharedKey = "\"$password\""
        }
        legacyAddedNetworkId = wifiManager.addNetwork(config)
        if (legacyAddedNetworkId == -1) {
            Log.e(TAG, "V2WifiAp: addNetwork failed (legacy path)")
            return
        }

        wifiManager.disconnect()
        wifiManager.enableNetwork(legacyAddedNetworkId, true)
        wifiManager.reconnect()

        // Best-effort: poll until associated, up to CONNECT_TIMEOUT_MS.
        val deadline = System.currentTimeMillis() + CONNECT_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            val info = wifiManager.connectionInfo
            if (info?.networkId == legacyAddedNetworkId && info.ssid?.trim('"') == ssid) {
                Log.d(TAG, "V2WifiAp: legacy SoftAP connected")
                return
            }
            delay(500)
        }
        Log.e(TAG, "V2WifiAp: legacy SoftAP connect timed out")
    }

    @SuppressLint("MissingPermission")
    private fun disconnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectivityManager.bindProcessToNetwork(null)
            modernCallback?.let {
                runCatching { connectivityManager.unregisterNetworkCallback(it) }
                modernCallback = null
            }
        } else {
            @Suppress("DEPRECATION")
            try {
                if (legacyAddedNetworkId != -1) wifiManager.removeNetwork(legacyAddedNetworkId)
                if (legacyPreviousNetworkId != -1) {
                    wifiManager.enableNetwork(legacyPreviousNetworkId, true)
                    wifiManager.reconnect()
                }
            } catch (e: Exception) {
                Log.w(TAG, "V2WifiAp: legacy restore failed", e)
            }
            legacyAddedNetworkId = -1
            legacyPreviousNetworkId = -1
        }
    }
}
