package pl.llp.aircasting.data.local

import android.content.Context
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.LogoutEvent
import pl.llp.aircasting.util.events.SessionsSyncEvent
import pl.llp.aircasting.util.extensions.safeRegister
import javax.inject.Inject

class LogoutService @Inject constructor(
    private val mSettings: Settings,
    private val appContext: Context
) {
    fun logout(callback: ((Unit) -> Unit)? = null) {
        EventBus.getDefault().safeRegister(this)
        // to make sure downloading sessions stopped before we start deleting them
        EventBus.getDefault().post(LogoutEvent())
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

    @Subscribe
    fun onMessageEvent(sync: SessionsSyncEvent) {
        if (!sync.inProgress) {
            EventBus.getDefault().unregister(this)
            LoginActivity.startAfterSignOut(appContext)
            mSettings.logout()
            clearDatabase()
        }
    }
}
