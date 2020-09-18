package io.lunarlogic.aircasting.screens.map

import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.events.ApplicationClosed
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings

import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.ConnectivityManager
import io.lunarlogic.aircasting.screens.new_session.LoginActivity
import io.lunarlogic.aircasting.sensor.SessionManager
import org.greenrobot.eventbus.EventBus

class MapController(
    private val rootActivity: AppCompatActivity,
    private val mViewMvc: MapViewMvc
) {
    fun onCreate() {

    }

    fun onDestroy() {

    }
}
