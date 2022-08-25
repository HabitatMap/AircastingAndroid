package pl.llp.aircasting.ui.view.screens.settings

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.dialog_backend_settings_confirmation.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseDialog

class BackendSettingsConfirmationDialog(
    mFragmentManager: FragmentManager,
    private val listener: SettingsViewMvc.BackendSettingsDialogListener,
    private val mUrl: String?,
    private val mPort: String?
) : BaseDialog(mFragmentManager) {
    private lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.dialog_backend_settings_confirmation, null)

        mView.ok_button.setOnClickListener {
            if (mUrl != null && mPort != null) {
                listener.confirmClicked(mUrl, mPort)
            }
        }

        mView.cancel_button.setOnClickListener {
            dismiss()
        }

        return mView
    }
}
