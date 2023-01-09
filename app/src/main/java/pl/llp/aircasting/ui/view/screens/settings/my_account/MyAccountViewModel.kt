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

    fun deleteAccount() = viewModelScope.launch {
        logoutService.deleteAccount()
            .onSuccess {
                if (it.body()?.success == false) return@onSuccess

                logout()
                Log.v("DeleteAccount", "success")

            }
            .onFailure {
                errorHandler.handleAndDisplay(Account.DeleteError(it))
            }
    }
}
