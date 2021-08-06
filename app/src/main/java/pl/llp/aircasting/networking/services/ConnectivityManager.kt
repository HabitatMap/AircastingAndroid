package pl.llp.aircasting.networking.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

// I know it's deprecated, but it's needed to support Android 5.0
import android.net.NetworkInfo

import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.Settings

class ConnectivityManager(apiService: ApiService, context: Context, settings: Settings): BroadcastReceiver() {
    val sessionSyncService = SessionsSyncService.get(apiService, ErrorHandler(context), settings, context)

    companion object {
        val ACTION = ConnectivityManager.CONNECTIVITY_ACTION

        fun isConnected(context: Context?): Boolean {
            val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            return activeNetwork?.isConnectedOrConnecting == true
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (!isInitialStickyBroadcast && isConnected(context)) {
            sessionSyncService.sync()
        }
    }
}
