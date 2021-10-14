package pl.llp.aircasting.screens.settings.my_account

import android.content.Context
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.screens.new_session.LoginActivity
import pl.llp.aircasting.database.LogoutService
import pl.llp.aircasting.lib.AppBar

class MyAccountController(
    private val mContext: Context,
    private val mViewMvc: MyAccountViewMvc,
    private val mSettings: Settings
) : MyAccountViewMvc.Listener {
    private val logoutService = LogoutService(mSettings)

    fun onStart(){
        mViewMvc.registerListener(this)
        mViewMvc.bindAccountDetail(mSettings.getEmail())
    }

    fun onResume() {
        AppBar.adjustMenuVisibility(false)
    }

    fun onStop(){
        mViewMvc.unregisterListener(this)
    }

    override fun onSignOutClicked() {
        logoutService.perform {
            LoginActivity.startAfterSignOut(mContext)
        }
    }

}
