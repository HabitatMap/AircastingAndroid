package pl.llp.aircasting.ui.view.screens.dashboard

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.confirmation_dialog.view.*
import kotlinx.android.synthetic.main.network_password_dialog.view.ok_button
import kotlinx.android.synthetic.main.session_actions_modifiable.view.cancel_button
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseDialog

open class ConfirmDangerCodeActionDialog(
    private val mFragmentManager: FragmentManager?,
    private val email: String? = "",
    private val okCallback: () -> (Unit),
) : BaseDialog(mFragmentManager) {
    constructor() : this(null, null, {})

    private lateinit var mView: View
    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.confirm_code_dialog, null)
        mView.ok_button.setOnClickListener {
            okButtonClicked()
        }
        mView.cancel_button.setOnClickListener {
            dismiss()
        }
        mView.confirmation_dialog_header.text = getString(R.string.my_account_delete_confirmation_code).format(email)
        return mView
    }

    private fun okButtonClicked() {
        okCallback()
        dismiss()
    }
}
