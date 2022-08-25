package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseDialog
import kotlinx.android.synthetic.main.network_password_dialog.view.*
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.NewSessionViewMvc

class CameraPermissionHelperDialog(
    private val mFragmentManager: FragmentManager,
    private val listener: AddNoteBottomSheet.Listener,
) : BaseDialog(mFragmentManager) {
    private lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.camera_permission_helper_dialog, null)

        mView.ok_button.setOnClickListener { okButtonClicked() }
        mView.cancel_button.setOnClickListener { dismiss() }

        return mView
    }

    private fun okButtonClicked() {
        listener.okButtonClickedFromDialog()
        dismiss()
    }
}
