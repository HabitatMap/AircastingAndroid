package pl.llp.aircasting.ui.view.screens.sync

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.sync_unavailable_dialog.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseDialog


class SyncUnavailableDialog (
    private val mFragmentManager: FragmentManager
    ): BaseDialog(mFragmentManager) {
        private lateinit var mView: View
        override fun setupView(inflater: LayoutInflater): View {
            mView = inflater.inflate(R.layout.sync_unavailable_dialog, null)
            mView.continue_button.setOnClickListener {
                dismiss()
            }
            return mView
        }
}
