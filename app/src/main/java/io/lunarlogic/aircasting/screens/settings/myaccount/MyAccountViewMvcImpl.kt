package io.lunarlogic.aircasting.screens.settings.myaccount

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.new_session.LoginViewMvc
import kotlin.math.sign

class MyAccountViewMvcImpl : BaseObservableViewMvc<MyAccountViewMvc.Listener>, MyAccountViewMvc {

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?
    ) : super(){
        this.rootView = inflater.inflate(R.layout.activity_myaccount, parent, false)

        val signOutButton = findViewById<Button>(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            signOutClicked()
        }
    }

    private fun signOutClicked() {
        for(listener in listeners){
            listener.onSignOutClicked()
        }
    }

}