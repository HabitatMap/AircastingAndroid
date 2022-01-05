package pl.llp.aircasting.screens.settings.my_account

import android.content.Context
import android.view.WindowManager
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.screens.new_session.LoginActivity
import pl.llp.aircasting.database.LogoutService
import pl.llp.aircasting.lib.AppBar
<<<<<<< HEAD:app/src/main/java/pl/llp/aircasting/screens/settings/my_account/MyAccountController.kt
import pl.llp.aircasting.screens.common.BaseController
=======
>>>>>>> ed7f3f27 (rebase 6):app/src/main/java/pl/llp/aircasting/screens/settings/clear_sd_card/my_account/MyAccountController.kt

class MyAccountController(
    private val mContext: Context,
    mViewMvc: MyAccountViewMvcImpl,
    private val mSettings: Settings
) : BaseController<MyAccountViewMvcImpl>(mViewMvc), MyAccountViewMvc.Listener {
    private val logoutService = LogoutService(mSettings)

    fun onStart(){
        mViewMvc?.registerListener(this)
        mViewMvc?.bindAccountDetail(mSettings.getEmail())
    }

    fun onResume() {
        AppBar.adjustMenuVisibility(false)
    }

    fun onStop(){
        mViewMvc?.unregisterListener(this)
    }

    override fun onSignOutClicked() {
        logoutService.perform {
            LoginActivity.startAfterSignOut(mContext)
        }
    }

}
