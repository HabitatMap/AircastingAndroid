package pl.llp.aircasting.database

import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.events.LogoutEvent
import pl.llp.aircasting.lib.Settings

class LogoutService(private val mSettings: Settings) {
    fun perform(callback: (() -> Unit)? = null) {
        // to make sure downloading sessions stopped before we start deleting them
        EventBus.getDefault().post(LogoutEvent())
        mSettings.logout()
        clearDatabase()
        callback?.invoke()
    }

    @OptIn(DelicateCoroutinesApi::class)
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
