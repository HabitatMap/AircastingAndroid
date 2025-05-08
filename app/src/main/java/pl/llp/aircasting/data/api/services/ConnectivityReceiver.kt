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
import javax.inject.Inject

@UserSessionScope
class ConnectivityReceiver @Inject constructor(
    private val sessionSyncService: SessionsSyncService,
    @IoCoroutineScope private val coroutineScope: CoroutineScope,
) : BroadcastReceiver() {

    companion object {
        const val ACTION = ConnectivityManager.CONNECTIVITY_ACTION
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "On receive triggered\n" +
                "Context: $context\n" +
                "${!isInitialStickyBroadcast}, ${context.isConnected}")
        if (!isInitialStickyBroadcast && context.isConnected) {
            Log.d(TAG, "Launching sync")
            coroutineScope.launch {
                sessionSyncService.sync()
            }
        }
    }
}
