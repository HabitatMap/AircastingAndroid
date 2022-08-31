package pl.llp.aircasting.ui.view.screens.new_session.session_details

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.network_password_dialog.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseDialog

class NetworkPasswordDialog(
    private val wifiSSID: String,
    private val mFragmentManager: FragmentManager,
    private val listener: FixedSessionDetailsViewMvc.OnPasswordProvidedListener,
    private val onDismissListener: OnDismissListener
) : BaseDialog(mFragmentManager) {
    interface OnDismissListener {
        fun onDismiss()
    }
    private lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.network_password_dialog, null)

        mView.wifi_password_dialog_header.text = requireActivity()
            .getString(R.string.fixed_session_details_wifi_password_dialog_header)
            .format(wifiSSID)

        mView.wifi_ssid_input.setText(wifiSSID)

        mView.ok_button.setOnClickListener {
            okButtonClicked()
        }

        mView.cancel_button.setOnClickListener {
            close()
        }

        return mView
    }

    private fun okButtonClicked() {
        val password = mView.wifi_password_input.text.toString().trim()
        listener.onNetworkPasswordProvided(password)

        close()
    }

    private fun close() {
        dismiss()
        onDismissListener.onDismiss()
    }
}
