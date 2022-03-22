package pl.llp.aircasting.screens.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.common.BaseDialog

class ThemeSettingsDialog(
    mFragmentManager: FragmentManager,
    private val mContext: Context?
) : BaseDialog(mFragmentManager) {
    private lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.theme_settings_dialog, null)

        mView.setOnClickListener { dismiss() }

        return mView
    }

}
