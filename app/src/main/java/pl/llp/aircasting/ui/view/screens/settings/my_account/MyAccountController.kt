package pl.llp.aircasting.ui.view.screens.settings.my_account

import android.content.Context
import pl.llp.aircasting.data.local.LogoutService
import pl.llp.aircasting.ui.view.common.BaseController
import pl.llp.aircasting.util.Settings


class MyAccountController(
    private val mContext: Context,
    mViewMvc: MyAccountViewMvcImpl,
    private val mSettings: Settings,
    private val logoutService: LogoutService
) : BaseController<MyAccountViewMvcImpl>(mViewMvc), MyAccountViewMvc.Listener {

    fun onStart() {
        mViewMvc?.registerListener(this)
        mViewMvc?.bindAccountDetail(mSettings.getEmail())
    }

    fun onStop() {
        mViewMvc?.unregisterListener(this)
    }

    override fun onSignOutClicked() {
        logoutService.perform {
            LoginActivity.startAfterSignOut(mContext)
        }
    }
}
