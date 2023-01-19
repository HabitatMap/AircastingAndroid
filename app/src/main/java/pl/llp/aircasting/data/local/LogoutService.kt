package pl.llp.aircasting.data.local

import android.content.Context
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.params.DeleteAccountResponse
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.LogoutEvent
import pl.llp.aircasting.util.events.SessionsSyncEvent
import pl.llp.aircasting.util.extensions.runOnIOThread
import pl.llp.aircasting.util.extensions.safeRegister
import retrofit2.Response
import javax.inject.Inject

class LogoutService @Inject constructor(
    private val mSettings: Settings,
    private val appContext: Context,
    private val apiServiceFactory: ApiServiceFactory
) {
    fun logout(afterAccountDeletion: Boolean = false) {
        EventBus.getDefault().safeRegister(this)
        // to make sure downloading sessions stopped before we start deleting them
        LoginActivity.startAfterSignOut(appContext)

        if (afterAccountDeletion) {
            EventBus.getDefault().postSticky(LogoutEvent(isAfterAccountDeletion = true))
            finaliseLogout()
        }
        else
            EventBus.getDefault().postSticky(LogoutEvent())
    }

    suspend fun deleteAccount(): Result<Response<DeleteAccountResponse?>> {
        val apiServiceAuthenticated = apiServiceFactory.get(mSettings.getAuthToken())
        return runCatching { apiServiceAuthenticated.deleteAccount() }
    }

    private fun clearDatabase() {
        Thread.sleep(1000)

        runOnIOThread { DatabaseProvider.get().clearAllTables() }
    }

    @Subscribe
    fun onMessageEvent(sync: SessionsSyncEvent) {
        if (!sync.inProgress) {
            finaliseLogout()
        }
    }

    private fun finaliseLogout() {
        EventBus.getDefault().unregister(this)
        mSettings.logout()
        clearDatabase()
        SessionsSyncService.destroy()
        EventBus.getDefault().postSticky(LogoutEvent(false))
    }
}
