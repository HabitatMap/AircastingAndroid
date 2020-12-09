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
    private val mFragmentManager : FragmentManager,
    private val mSettings : Settings
) : BaseDialog(mFragmentManager) {
    private lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.backend_settings_dialog, null)

        mView.ok_button.setOnClickListener {
            settingsConfirmed()
        }

        mView.cancel_button.setOnClickListener {
            dismiss()
        }

        return mView
    }

    private fun settingsConfirmed() {
        val addressValue = mView.url_input.text.toString() // TODO
        val portValue = mView.port_input.text.toString()
        mSettings.backendSettingsChanged(addressValue, portValue)
        Log.i("SETTINGS_DIALOG", "Adress and port values added to sharedPreferences")
        dismiss()
    }
}
