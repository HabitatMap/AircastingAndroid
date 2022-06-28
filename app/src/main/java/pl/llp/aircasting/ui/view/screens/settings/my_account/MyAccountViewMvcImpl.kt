package pl.llp.aircasting.ui.view.screens.settings.my_account

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc
import kotlinx.android.synthetic.main.activity_myaccount.view.*

class MyAccountViewMvcImpl : BaseObservableViewMvc<MyAccountViewMvc.Listener>, MyAccountViewMvc {

    private val mContext: Context
    private var headerTextView: TextView?

    constructor(
        context: Context,
        inflater: LayoutInflater, parent: ViewGroup?
    ) : super(){
        this.rootView = inflater.inflate(R.layout.activity_myaccount, parent, false)
        mContext = context

        val signOutButton = rootView?.sign_out_button
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
