package io.lunarlogic.aircasting.screens.settings

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseDialog
import kotlinx.android.synthetic.main.microphone_settings_dialog.view.*

class MicrophoneSettingsDialog(
    mFragmentManager : FragmentManager,
    private val micValue: String?,
    private val listener: SettingsViewMvc.MicrophoneSettingsDialogListener
    ) : BaseDialog(mFragmentManager) {
    private lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.microphone_settings_dialog, null)

        mView.mic_setting_input.setText(micValue)

        mView.ok_button.setOnClickListener {
            microphoneSettingsConfirmed()
        }

        mView.cancel_button.setOnClickListener {
            dismiss()
        }

        return mView
    }

    private fun microphoneSettingsConfirmed(){
        val micValue = mView.mic_setting_input.toString().trim()
        listener.confirmMicrophoneSettingsClicked(micValue)
        dismiss()
    }


}
