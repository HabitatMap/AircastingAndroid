package pl.llp.aircasting.ui.view.screens.new_session

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.confirmation_dialog.view.cancel_button
import kotlinx.android.synthetic.main.confirmation_dialog.view.confirmation_dialog_description
import kotlinx.android.synthetic.main.confirmation_dialog.view.confirmation_dialog_header
import kotlinx.android.synthetic.main.confirmation_dialog.view.ok_button
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseDialog

class FixedSessionMisconfiguredDialog(
    fragmentManager: FragmentManager?,
    private val onOkClicked: () -> Unit,
) : BaseDialog(fragmentManager) {

    override fun setupView(inflater: LayoutInflater): View {
        val view = inflater.inflate(R.layout.confirmation_dialog, null)
        view.confirmation_dialog_header.text = getString(R.string.fixed_session_misconfigured_dialog_header)
        view.confirmation_dialog_description.text = getString(R.string.fixed_session_misconfigured_dialog_description)
        view.confirmation_dialog_description.visibility = View.VISIBLE
        view.cancel_button.visibility = View.GONE
        isCancelable = false
        view.ok_button.setOnClickListener {
            onOkClicked()
            dismiss()
        }
        return view
    }
}
