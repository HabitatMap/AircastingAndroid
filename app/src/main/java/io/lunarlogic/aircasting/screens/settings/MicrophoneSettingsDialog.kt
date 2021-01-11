package io.lunarlogic.aircasting.screens.settings

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.common.BaseDialog

class MicrophoneSettingsDialog(
    mFragmentManager : FragmentManager,
    private val listener: SettingsViewMvc.MicrophoneSettingsDialogListener
    ) : BaseDialog(mFragmentManager) {

    override fun setupView(inflater: LayoutInflater): View {
        TODO("Not yet implemented")
    }


}
