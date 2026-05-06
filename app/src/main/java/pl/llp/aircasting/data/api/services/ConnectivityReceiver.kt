package pl.llp.aircasting.data.api.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.util.extensions.isConnected
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import javax.inject.Inject

@UserSessionScope
class ConnectivityReceiver @Inject constructor(
    private val sessionSyncService: SessionsSyncService,
    private val v2StateRepository: AirBeamMiniV2StateRepository,
    @IoCoroutineScope private val coroutineScope: CoroutineScope,
) : BroadcastReceiver() {

    companion object {
        const val ACTION = ConnectivityManager.CONNECTIVITY_ACTION
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "On receive triggered\n" +
                "Context: $context\n" +
                "${!isInitialStickyBroadcast}, ${context.isConnected}")
        // The V2 manual-sync orchestrator briefly puts the phone on the AirBeam SoftAP
        // (no INTERNET). Android still fires CONNECTIVITY_ACTION around that switch and
        // the system default network can briefly point at the SoftAP — running
        // SessionsSyncService.sync() then sprays failed DNS/HTTP attempts into ESP32's
        // 4-socket pool, racing the in-flight /sync download. Skip the auto-sync while
        // V2 sync is in progress; ConnectivityReceiver will re-fire when the AP is
        // released and the default network returns to normal.
        if (v2StateRepository.syncInProgress) {
            Log.d(TAG, "Skipping sync — V2 manual sync in progress")
            return
        }
        if (!isInitialStickyBroadcast && context.isConnected) {
            Log.d(TAG, "Launching sync")
            coroutineScope.launch {
                sessionSyncService.sync()
            }
        }
    }
}
