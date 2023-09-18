package pl.llp.aircasting.ui.view.screens.new_session.confirmation

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.camera_permission_helper_dialog.view.dont_allow_btn
import kotlinx.android.synthetic.main.camera_permission_helper_dialog.view.ok_permission_btn
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseDialog

class NotificationPermissionDialog(
    mFragmentManager: FragmentManager,
    private val okButtonCallback: () -> (Unit),
    private val dismissButtonCallback: () -> (Unit),
) : BaseDialog(mFragmentManager) {

    private lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.notification_permission_dialog, null)

        mView.ok_permission_btn.setOnClickListener { okButtonClicked() }
        mView.dont_allow_btn.setOnClickListener { dismissButtonClicked() }

        return mView
    }

    private fun okButtonClicked() {
        okButtonCallback()
        dismiss()
    }

    private fun dismissButtonClicked(){
        dismissButtonCallback()
        dismiss()
    }
}
