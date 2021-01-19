package io.lunarlogic.aircasting.screens.settings.myaccount

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import kotlinx.android.synthetic.main.activity_myaccount.view.*

class MyAccountViewMvcImpl : BaseObservableViewMvc<MyAccountViewMvc.Listener>, MyAccountViewMvc {

    private val mContext: Context
    private var headerTextView: TextView?
    private var signOutButton: Button?

    constructor(
        context: Context,
        inflater: LayoutInflater, parent: ViewGroup?
    ) : super(){
        this.rootView = inflater.inflate(R.layout.activity_myaccount, parent, false)
        mContext = context

        signOutButton = rootView?.sign_out_button
        signOutButton?.setOnClickListener {
            signOutClicked()
        }

        headerTextView = rootView?.header
    }

    private fun signOutClicked() {
        for (listener in listeners) {
            listener.onSignOutClicked()
        }
    }

    override fun bindAccountDetail(email : String?){
        headerTextView?.text = mContext.getString(R.string.my_account_info).format(email)
    }
}
