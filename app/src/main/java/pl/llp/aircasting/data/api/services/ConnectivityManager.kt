package pl.llp.aircasting.data.api.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.isConnected

class ConnectivityManager(
    apiService: ApiService,
    context: Context,
    settings: Settings,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) :
    BroadcastReceiver() {
    private val sessionSyncService =
        SessionsSyncService.get(apiService, ErrorHandler(context))

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
