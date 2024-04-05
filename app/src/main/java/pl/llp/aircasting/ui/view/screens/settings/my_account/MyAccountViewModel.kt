package pl.llp.aircasting.ui.view.screens.settings.my_account

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.llp.aircasting.data.local.LogoutService
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.Account
import pl.llp.aircasting.util.exceptions.ErrorHandler
import javax.inject.Inject


class MyAccountViewModel @Inject constructor(
    private val mSettings: Settings,
    private val logoutService: LogoutService,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    val userName get() = mSettings.getEmail()

    fun logout() {
        logoutService.logout()
    }

    fun deleteAccountSendEmail() = viewModelScope.launch {
        logoutService.deleteAccountSendEmail()
            .onSuccess {

                Log.v("marta", "marta")

            }
            .onFailure {
                errorHandler.handleAndDisplay(Account.DeleteError(it))
            }
    }

    fun deleteAccountConfirmCode(code: String?) = viewModelScope.launch {
        code?.let {
            logoutService.deleteAccountConfirmCode(code)
                .onSuccess {
                    // czemu 401 dawało on success?
                    logoutService.logout(true)
                    Log.v("DeleteAccount", "success")

                }
                .onFailure {
                    errorHandler.handleAndDisplay(Account.DeleteError(it))
                }
        } ?: run {
            // display error? shouldn't happen?
        }
    }
}
