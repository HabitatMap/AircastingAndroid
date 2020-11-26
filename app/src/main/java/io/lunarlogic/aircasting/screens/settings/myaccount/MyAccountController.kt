package io.lunarlogic.aircasting.screens.settings.myaccount

import io.lunarlogic.aircasting.screens.settings.SettingsViewMvc

class MyAccountController(
    private val mViewMvc: MyAccountViewMvc
) : MyAccountViewMvc.Listener{

    fun onStart(){
        mViewMvc.registerListener(this)
    }

    fun onStop(){
        mViewMvc.unregisterListener(this)
    }

    override fun onSignOutClicked() {
        // Todo: clearing out settings and room tables
        // Todo: Go to Login Activity <?>
    }


}