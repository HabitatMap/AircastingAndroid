package pl.llp.aircasting.permissions

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.backend_settings_dialog.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.common.BaseDialog

class LocationPermissionPopUp(
    mFragmentManager : FragmentManager,
    private val permissionsManager: PermissionsManager,
    private val mContextActivity: AppCompatActivity
) : BaseDialog(mFragmentManager) {
    private lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.location_permission_popup, null)

        mView.ok_button.setOnClickListener {
            permissionsManager.requestLocationPermissions(mContextActivity)
            dismiss()
        }

        return mView
    }


}
