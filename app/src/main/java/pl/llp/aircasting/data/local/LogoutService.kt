package pl.llp.aircasting.data.local

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.params.DeleteAccountResponse
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.Authenticated
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.LogoutEvent
import pl.llp.aircasting.util.extensions.runOnIOThread
import retrofit2.Response
import javax.inject.Inject

@UserSessionScope
class LogoutService @Inject constructor(
    private val mSettings: Settings,
    private val app: AircastingApplication,
    @Authenticated private val apiService: ApiService,
    private val sessionsSyncService: SessionsSyncService,
    @IoCoroutineScope private val coroutineScope: CoroutineScope,
) {
    fun logout(afterAccountDeletion: Boolean = false) {
        // to make sure downloading sessions stopped before we start deleting them
        LoginActivity.startAfterSignOut(app.applicationContext)

        EventBus.getDefault().postSticky(LogoutEvent())
        coroutineScope.launch {
            if (!afterAccountDeletion) {
                sessionsSyncService.sync()
            }
            finaliseLogout()
        }
    }

    suspend fun deleteAccount(): Result<Response<DeleteAccountResponse?>> {
        return runCatching { apiService.deleteAccount() }
    }

    private fun finaliseLogout() {
        EventBus.getDefault().unregister(this)
        mSettings.logout()
        clearDatabase()
        app.userDependentComponent = null
        EventBus.getDefault().postSticky(LogoutEvent(false))
    }
    private fun clearDatabase() {
        Thread.sleep(1000)

        Log.d(TAG, "Clearing database")
        runOnIOThread { DatabaseProvider.get().clearAllTables() }
    }
}
