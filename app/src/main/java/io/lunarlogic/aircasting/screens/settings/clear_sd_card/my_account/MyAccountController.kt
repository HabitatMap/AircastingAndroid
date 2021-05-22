package io.lunarlogic.aircasting.screens.settings.clear_sd_card.my_account

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.events.LogoutEvent
import io.lunarlogic.aircasting.lib.AuthenticationHelper
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.new_session.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus

class MyAccountController(
    private val mContext: Context,
    private val mViewMvc: MyAccountViewMvc,
    private val mSettings: Settings
) : MyAccountViewMvc.Listener{
    private val mAuthenticationHelper = AuthenticationHelper(mContext)

    fun onStart(){
        mViewMvc.registerListener(this)
        mViewMvc.bindAccountDetail(mAuthenticationHelper.getEmail())
    }

    fun onStop(){
        mViewMvc.unregisterListener(this)
    }

    override fun onSignOutClicked() {
        EventBus.getDefault().post(LogoutEvent())

        mSettings.logout()
        mAuthenticationHelper.removeAccount()
//      todo:  accountManager.setAuthToken(null) ??
//      todo:  accountManager.setPassword(null) ??
        clearDatabase()

        LoginActivity.startAfterSignOut(mContext)
    }

    private fun clearDatabase() {
        // to make sure downloading sessions stopped before we start deleting them
        Thread.sleep(1000)
        runBlocking {
            val query = GlobalScope.async(Dispatchers.IO) {
                DatabaseProvider.get().clearAllTables()
            }
            query.await()
        }
    }
}
