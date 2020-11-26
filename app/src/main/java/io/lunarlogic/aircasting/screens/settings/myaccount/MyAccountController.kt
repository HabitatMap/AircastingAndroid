package io.lunarlogic.aircasting.screens.settings.myaccount

import android.content.Context
import androidx.room.Database
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.new_session.LoginActivity

class MyAccountController(
    private val mContext: Context,
    private val mViewMvc: MyAccountViewMvc,
    private val mSettings: Settings
) : MyAccountViewMvc.Listener{

    fun onStart(){
        mViewMvc.registerListener(this)
    }

    fun onStop(){
        mViewMvc.unregisterListener(this)
    }

    override fun onSignOutClicked() {
        // Todo: check if below lines are fine
        mSettings.logout()
        DatabaseProvider.mAppDatabase?.clearAllTables()
        LoginActivity.start(mContext)
    }

}