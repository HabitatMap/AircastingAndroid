package pl.llp.aircasting.screens.settings.clear_sd_card.my_account

import android.content.Context
import pl.llp.aircasting.events.LogoutEvent
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.screens.new_session.LoginActivity
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.database.ClearDatabaseService

class MyAccountController(
    private val mContext: Context,
    private val mViewMvc: MyAccountViewMvc,
    private val mSettings: Settings
) : MyAccountViewMvc.Listener{
    private val clearDatabaseService = ClearDatabaseService()

    fun onStart(){
        mViewMvc.registerListener(this)
        mViewMvc.bindAccountDetail(mSettings.getEmail())
    }

    fun onStop(){
        mViewMvc.unregisterListener(this)
    }

    override fun onSignOutClicked() {
        EventBus.getDefault().post(LogoutEvent())

        mSettings.logout()
        clearDatabaseService.perform()

        LoginActivity.startAfterSignOut(mContext)
    }

}
