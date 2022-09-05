package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.camera_permission_helper_dialog.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseDialog

class CameraPermissionHelperDialog(
    mFragmentManager: FragmentManager,
    private val okButtonCallback: () -> (Unit)
) : BaseDialog(mFragmentManager) {

    private lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.camera_permission_helper_dialog, null)

        mView.ok_permission_btn.setOnClickListener { okButtonClicked() }
        mView.dont_allow_btn.setOnClickListener { dismiss() }

        return mView
    }

    private fun okButtonClicked() {
        okButtonCallback()
        dismiss()
    }
}
