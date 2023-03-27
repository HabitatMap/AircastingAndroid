package pl.llp.aircasting.data.local

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.data.api.params.DeleteAccountResponse
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.LogoutEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.runOnIOThread
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogoutService @Inject constructor(
    private val mSettings: Settings,
    private val appContext: Context,
    private val apiServiceFactory: ApiServiceFactory,
    private val errorHandler: ErrorHandler,
    private val sessionsSyncService: SessionsSyncService = SessionsSyncService.get(
        apiServiceFactory.getAuthenticated(
            mSettings.getAuthToken()
        ), errorHandler
    ),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    fun logout(afterAccountDeletion: Boolean = false) {
        // to make sure downloading sessions stopped before we start deleting them
        LoginActivity.startAfterSignOut(appContext)

        EventBus.getDefault().postSticky(LogoutEvent())
        coroutineScope.launch {
            if (!afterAccountDeletion) {
                sessionsSyncService.sync()
            }
            finaliseLogout()
        }
    }

    suspend fun deleteAccount(): Result<Response<DeleteAccountResponse?>> {
        val apiServiceAuthenticated = apiServiceFactory.getAuthenticated(mSettings.getAuthToken())
        return runCatching { apiServiceAuthenticated.deleteAccount() }
    }

    private fun finaliseLogout() {
        EventBus.getDefault().unregister(this)
        mSettings.logout()
        clearDatabase()
        SessionsSyncService.destroy()
        EventBus.getDefault().postSticky(LogoutEvent(false))
    }
    private fun clearDatabase() {
        Thread.sleep(1000)

        Log.d(TAG, "Clearing database")
        runOnIOThread { DatabaseProvider.get().clearAllTables() }
    }
}
