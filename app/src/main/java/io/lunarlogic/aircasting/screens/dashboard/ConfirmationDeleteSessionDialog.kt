package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseDialog
import kotlinx.android.synthetic.main.network_password_dialog.view.*
import kotlinx.android.synthetic.main.session_actions.view.cancel_button

class ConfirmationDeleteSessionDialog(
    private val mFragmentManager: FragmentManager,
    val okCallback: () -> (Unit)
): BaseDialog(mFragmentManager) {
    private lateinit var mView: View
    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.confirmation_dialog, null)
        mView.ok_button.setOnClickListener {
            okButtonClicked()
        }
        mView.cancel_button.setOnClickListener {
            dismiss()
        }
        return mView
    }
    private fun okButtonClicked() {
        okCallback()
        dismiss()
    }
}
