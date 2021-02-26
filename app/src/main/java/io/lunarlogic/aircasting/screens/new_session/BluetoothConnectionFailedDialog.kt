package io.lunarlogic.aircasting.screens.new_session

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseDialog
import kotlinx.android.synthetic.main.microphone_settings_dialog.view.*

class BluetoothConnectionFailedDialog(
    mFragmentManager : FragmentManager
) : BaseDialog(mFragmentManager) {
    lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.bluetooth_connection_fail_dialog, null)

        mView.ok_button.setOnClickListener {
            dismiss()
        }

        return mView
    }
}
