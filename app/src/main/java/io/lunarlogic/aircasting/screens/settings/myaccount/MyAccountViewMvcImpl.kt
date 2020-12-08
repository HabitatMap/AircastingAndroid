package io.lunarlogic.aircasting.screens.settings.myaccount

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import kotlinx.android.synthetic.main.network_password_dialog.view.*

class MyAccountViewMvcImpl : BaseObservableViewMvc<MyAccountViewMvc.Listener>, MyAccountViewMvc {

    private val mContext: Context
    private var loginStateTextView: TextView

    constructor(
        context: Context,
        inflater: LayoutInflater, parent: ViewGroup?
    ) : super(){
        this.rootView = inflater.inflate(R.layout.activity_myaccount, parent, false)
        mContext = context

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
        loginStateTextView.text = mContext.getString(R.string.my_account_info).format(email)
    }
}
