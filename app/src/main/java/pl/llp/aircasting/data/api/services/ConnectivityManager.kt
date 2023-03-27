package pl.llp.aircasting.data.api.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.llp.aircasting.util.extensions.isConnected
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityManager @Inject constructor(
    private val sessionSyncService: SessionsSyncService,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : BroadcastReceiver() {

    companion object {
        const val ACTION = ConnectivityManager.CONNECTIVITY_ACTION

        fun isConnected(context: Context?): Boolean {
            return context?.isConnected ?: false
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (!isInitialStickyBroadcast && isConnected(context)) {
            coroutineScope.launch {
                sessionSyncService.sync()
            }
        }
    }
}
