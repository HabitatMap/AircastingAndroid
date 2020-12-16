package io.lunarlogic.aircasting.screens.settings.myaccount

import android.content.Context
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.SessionsSyncService
import io.lunarlogic.aircasting.screens.new_session.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
        mSettings.logout()
        SessionsSyncService.destroy()

        DatabaseProvider.runQuery {
            DatabaseProvider.get().clearAllTables()
        }
        LoginActivity.startAfterSignOut(mContext)
    }
}
