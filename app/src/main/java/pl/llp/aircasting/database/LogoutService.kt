package pl.llp.aircasting.database

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.events.LogoutEvent
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.networking.services.SessionsSyncService

class LogoutService(
    private val mSettings: Settings,
    private val mApplication: Application? = null
    ) {
    fun perform(callback: (() -> Unit)? = null) {
        // to make sure downloading sessions stopped before we start deleting them
        EventBus.getDefault().post(LogoutEvent())
        mSettings.logout()
        clearDatabase()
        callback?.invoke()
    }

    private fun clearDatabase() {
        Thread.sleep(1000)
        runBlocking {
            val query = GlobalScope.async(Dispatchers.IO) {
                DatabaseProvider.get().clearAllTables()
            }
            query.await()
        }
    }
}
