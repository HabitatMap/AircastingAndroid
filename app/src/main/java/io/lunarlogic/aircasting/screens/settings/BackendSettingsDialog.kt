package io.lunarlogic.aircasting.screens.settings

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.common.BaseDialog
import kotlinx.android.synthetic.main.backend_settings_dialog.view.*

class BackendSettingsDialog(
    mFragmentManager : FragmentManager,
    private val mSettings : Settings,
    private val mUrl: String,
    private val mPort: String
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
        mSettings.backendSettingsChanged(urlValue, portValue) // TODO: move to controller
        Log.i("SETTINGS_DIALOG", "Adress and port values added to sharedPreferences")
        dismiss()
    }
}
