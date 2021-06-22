package io.lunarlogic.aircasting.screens.settings

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseDialog
import kotlinx.android.synthetic.main.backend_settings_dialog.view.*

class BackendSettingsDialog(
    mFragmentManager : FragmentManager,
    private val mUrl: String?,
    private val mPort: String?,
    private val listener: SettingsViewMvc.BackendSettingsDialogListener
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
        BackendSettingsConfirmationDialog(requireFragmentManager(), listener, urlValue, portValue).show()
    }

}
