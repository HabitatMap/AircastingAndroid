package pl.llp.aircasting.ui.view.screens.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.backend_settings_dialog.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseDialog

class BackendSettingsDialog(
    mFragmentManager : FragmentManager,
    private val mUrl: String?,
    private val mPort: String?,
    private val mContext: Context?
) : BaseDialog(mFragmentManager) {
    private lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.backend_settings_dialog, null)

        mView.url_input.setText(mUrl)
        mView.port_input.setText(mPort)

        mView.ok_button.setOnClickListener {
            settingsConfirmed()
        }

        mView.cancel_button.setOnClickListener {
            dismiss()
        }

        return mView
    }

    private fun settingsConfirmed() {
        val urlValue = mView.url_input.text.toString().trim()
        val portValue = mView.port_input.text.toString().trim()

        if (!urlValue.startsWith("http://") && !urlValue.startsWith("https://"))
            showError()
        else if (mUrl != urlValue)
           // BackendSettingsConfirmationDialog(requireFragmentManager(), listener, urlValue, portValue).show()
        else
            dismiss()

    }

    private fun showError() {
        mView.url_input?.error = "Url must start from http:// or https://"
    }

}
