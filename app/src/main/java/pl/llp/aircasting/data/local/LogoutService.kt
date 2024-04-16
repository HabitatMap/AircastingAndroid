package pl.llp.aircasting.data.local

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.params.DeleteAccountConfirmationCodeBody
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
import retrofit2.Response
import javax.inject.Inject
import pl.llp.aircasting.data.api.response.*

@UserSessionScope
class LogoutService @Inject constructor(
    private val mSettings: Settings,
    private val app: AircastingApplication,
    @Authenticated private val apiService: ApiService,
    private val sessionsSyncService: SessionsSyncService,
    private val mDatabase: AppDatabase,
    @IoCoroutineScope private val ioScope: CoroutineScope,
) {
    fun logout(afterAccountDeletion: Boolean = false) {
        LoginActivity.startAfterSignOut(app.applicationContext)

        EventBus.getDefault().postSticky(LogoutEvent())
        ioScope.launch {
            if (!afterAccountDeletion) {
                sessionsSyncService.sync()
            }
            finaliseLogout()
        }
    }

    suspend fun deleteAccountSendEmail(): Result<Response<DeleteAccountSendEmailResponse?>> {
        return runCatching { apiService.deleteAccountSendEmail() }
    }


    suspend fun deleteAccountConfirmCode(code: String): Result<Response<DeleteAccountResponse?>> {
        return runCatching { apiService.deleteAccountConfirmCode(DeleteAccountConfirmationCodeBody(code)) }
    }

    private suspend fun finaliseLogout() {
        EventBus.getDefault().unregister(this)
        mSettings.logout()
        clearDatabase()
        app.userDependentComponent = null
        EventBus.getDefault().postSticky(LogoutEvent(false))
    }
    private suspend fun clearDatabase() {
        delay(1000)

        Log.d(TAG, "Clearing database")
        mDatabase.clearAllTables()
    }
}
