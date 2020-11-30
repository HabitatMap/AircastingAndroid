package io.lunarlogic.aircasting.screens.settings.myaccount

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class MyAccountViewMvcImpl : BaseObservableViewMvc<MyAccountViewMvc.Listener>, MyAccountViewMvc {

    private var loginStateTextView: TextView

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?
    ) : super(){
        this.rootView = inflater.inflate(R.layout.activity_myaccount, parent, false)

        val signOutButton = findViewById<Button>(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            signOutClicked()
        }

        loginStateTextView = findViewById(R.id.login_state_textView)

    }

    private fun signOutClicked() {
        for(listener in listeners){
            listener.onSignOutClicked()
        }
    }

    override fun bindAccountDetail(email : String?){
        loginStateTextView.text = "You are currently logged in as ${email}"
    }

}