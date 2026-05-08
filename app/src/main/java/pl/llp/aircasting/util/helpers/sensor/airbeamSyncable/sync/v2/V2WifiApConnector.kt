package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import pl.llp.aircasting.data.api.util.TAG

/**
 * Drives the V2 manual-sync SoftAP join via `WifiNetworkSpecifier` + the system Wi-Fi
 * picker. Picker pre-fills the SSID + password we received over BLE, so the user only
 * confirms — they never have to type credentials or risk picking the wrong network.
 *
 * Diagnostic logging: while the picker is open we periodically dump `wifiManager.scanResults`
 * filtered to the target SSID. That tells us whether the AP is in the framework's scan cache
 * (visibility issue is in the picker / specifier matching) or absent (visibility issue is in
 * scan throttle / beacon propagation).
 */
class V2WifiApConnector(
    private val applicationContext: Context,
    private val ssid: String = AP_SSID,
) {
    companion object {
        const val AP_SSID = "AirBeamMini Sync"
        private const val CONNECT_TIMEOUT_MS = 90_000L
        private const val PRE_REQUEST_DELAY_MS = 2_000L
        private const val SCAN_DUMP_INTERVAL_MS = 5_000L
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
    suspend fun <T> withApConnection(password: String, block: suspend (Network) -> T): T? = coroutineScope {
        val network = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectModern(password, this)
        } else {
            Log.e(TAG, "V2WifiAp: API < 29 not supported for V2 manual sync")
            null
        }

        if (network == null) {
            Log.e(TAG, "V2WifiAp: AP join failed — skipping HTTP step")
            disconnect()
            return@coroutineScope null
        }

        // Pin the whole process to the SoftAP for the duration of the HTTP transfer.
        // V2SyncFileDownloader already binds its own OkHttpClient via socketFactory, but on
        // Android 16 / Pixel that's not enough: with only a passive receive-wait on the
        // /sync socket, Android observes ~10s of zero app-level traffic and tears down the
        // ephemeral no-INTERNET network (`onLost` ~12s after `onAvailable`, then
        // "Software caused connection abort" from the kernel). Process-binding marks the
        // network as actively in use by the foreground process and keeps it alive while
        // we wait on ESP. Restored to null in finally so the rest of the app reverts to
        // the system default. ConnectivityReceiver is gated on `syncInProgress`, so the
        // background sync that previously fought ESP's 4-socket pool is suppressed for
        // this window.
        val previousBinding = connectivityManager.boundNetworkForProcess
        connectivityManager.bindProcessToNetwork(network)
        Log.d(TAG, "V2WifiAp: process bound to SoftAP network $network")
        try {
            block(network)
        } finally {
            runCatching { connectivityManager.bindProcessToNetwork(previousBinding) }
                .onFailure { Log.w(TAG, "V2WifiAp: restoring previous process binding failed: ${it.message}") }
            disconnect()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun connectModern(password: String, scope: kotlinx.coroutines.CoroutineScope): Network? {
        Log.d(TAG, "V2WifiAp: requesting SoftAP network ssid='$ssid' (passwordLen=${password.length})")
        Log.d(TAG, "V2WifiAp: wifi enabled=${wifiManager.isWifiEnabled}, scan-always=${wifiManager.isScanAlwaysAvailable}")

        // Initial scan-cache snapshot before any explicit scan we issue.
        dumpScanResults("pre-startScan")

        val scanStarted = runCatching { @Suppress("DEPRECATION") wifiManager.startScan() }.getOrElse { false }
        Log.d(TAG, "V2WifiAp: startScan() returned $scanStarted (false often means rate-limited or wifi off)")
        delay(PRE_REQUEST_DELAY_MS)
        dumpScanResults("post-startScan+delay")

        // Keep dumping scan results while picker is open so we can see if/when AP appears.
        val dumper: Job = scope.launch {
            while (isActive) {
                delay(SCAN_DUMP_INTERVAL_MS)
                dumpScanResults("picker-open")
            }
        }

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
                runCatching {
                    val lp = connectivityManager.getLinkProperties(network)
                    Log.d(TAG, "V2WifiAp: linkProperties on available — ${formatLinkProperties(lp)}")
                }.onFailure { Log.w(TAG, "V2WifiAp: linkProperties read failed: ${it.message}") }
                // Caller (`withApConnection`) handles process binding for the HTTP window
                // so Android 16 doesn't tear down the ephemeral no-INTERNET network for
                // "no observed traffic" while we wait on ESP. ConnectivityReceiver is
                // gated on V2 syncInProgress to keep background sync off the SoftAP
                // during that window.
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

            override fun onLinkPropertiesChanged(network: Network, lp: LinkProperties) {
                Log.d(TAG, "V2WifiAp: linkProperties changed on $network — ${formatLinkProperties(lp)}")
            }

            override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
                Log.d(TAG, "V2WifiAp: blockedStatus changed on $network — blocked=$blocked")
            }
        }
        modernCallback = callback
        connectivityManager.requestNetwork(request, callback, CONNECT_TIMEOUT_MS.toInt())

        val result = withTimeoutOrNull(CONNECT_TIMEOUT_MS) { deferred.await() }
        dumper.cancel()
        dumpScanResults("picker-closed")
        return result
    }

    /**
     * Log scan-result entries matching the target SSID (or a count of total entries when not
     * found). With FINE_LOCATION granted this enumerates fresh `wifiManager.scanResults`.
     */
    @SuppressLint("MissingPermission")
    private fun dumpScanResults(phase: String) {
        val results: List<ScanResult> = runCatching { wifiManager.scanResults ?: emptyList() }
            .getOrElse {
                Log.w(TAG, "V2WifiAp[$phase]: scanResults threw: ${it.message}")
                return
            }
        val total = results.size
        val matches = results.filter { it.SSID == ssid }
        if (matches.isEmpty()) {
            val sample = results.take(10).joinToString(", ") { "${it.SSID}(${it.frequency}MHz,${it.level}dBm)" }
            Log.d(TAG, "V2WifiAp[$phase]: '$ssid' NOT in scan cache (total=$total). Sample: $sample")
        } else {
            matches.forEach { sr ->
                Log.d(
                    TAG,
                    "V2WifiAp[$phase]: '$ssid' MATCH — bssid=${sr.BSSID} freq=${sr.frequency}MHz " +
                            "level=${sr.level}dBm caps=${sr.capabilities}",
                )
            }
        }
    }

    private fun formatLinkProperties(lp: LinkProperties?): String {
        if (lp == null) return "null"
        val addresses = lp.linkAddresses.joinToString(",") { it.address.hostAddress ?: "?" }
        val routes = lp.routes.joinToString(",") {
            "${it.destination}->${it.gateway?.hostAddress ?: "?"}"
        }
        val dns = lp.dnsServers.joinToString(",") { it.hostAddress ?: "?" }
        return "iface=${lp.interfaceName} addrs=[$addresses] routes=[$routes] dns=[$dns]"
    }

    private fun disconnect() {
        modernCallback?.let {
            runCatching { connectivityManager.unregisterNetworkCallback(it) }
            modernCallback = null
        }
    }
}
