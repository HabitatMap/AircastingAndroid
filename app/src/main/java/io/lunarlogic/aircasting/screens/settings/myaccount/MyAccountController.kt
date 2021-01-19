package io.lunarlogic.aircasting.screens.settings.myaccount

import android.content.Context
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.events.LogoutEvent
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.SessionsSyncService
import io.lunarlogic.aircasting.screens.new_session.LoginActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MyAccountController(
    private val mContext: Context,
    private val mViewMvc: MyAccountViewMvc,
    private val mSettings: Settings
) : MyAccountViewMvc.Listener{

    fun onStart(){
        mViewMvc.registerListener(this)
        mViewMvc.bindAccountDetail(mSettings.getEmail())
    }

    fun onStop(){
        mViewMvc.unregisterListener(this)
    }

    override fun onSignOutClicked() {
        val event = LogoutEvent()
        EventBus.getDefault().post(event)

        mSettings.logout()
        SessionsSyncService.cancel()
        SessionsSyncService.destroy()

        DatabaseProvider.runQuery {
            DatabaseProvider.get().clearAllTables()
        }

        Thread.sleep(3000)
        LoginActivity.startAfterSignOut(mContext)
    }
}
