package pl.llp.aircasting.ui.view.screens.settings

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.microphone_settings_dialog.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseDialog

class MicrophoneSettingsDialog(
    mFragmentManager: FragmentManager,
    private val calibration: Int,
) : BaseDialog(mFragmentManager) {
    private lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.microphone_settings_dialog, null)

        mView.mic_setting_input.setText(calibration.toString())

        mView.ok_button.setOnClickListener {
            microphoneSettingsConfirmed()
        }

        mView.cancel_button.setOnClickListener {
            dismiss()
        }

        return mView
    }

    private fun microphoneSettingsConfirmed() {
        val calibration = mView.mic_setting_input.text.toString().trim().toInt()
        //listener.confirmMicrophoneSettingsClicked(calibration)
        dismiss()
    }
}
