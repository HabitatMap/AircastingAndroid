package pl.llp.aircasting.ui.view.screens.sync.refreshed

import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.ui.view.common.BaseController

class RefreshedSessionsController(
    private val mFragmentManager: FragmentManager,
    var viewMvc: RefreshedSessionsViewMvcImpl?
) : BaseController<RefreshedSessionsViewMvcImpl>(viewMvc) {
    fun registerListener(listener: RefreshedSessionsViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: RefreshedSessionsViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }

    fun onBackPressed() {
        mFragmentManager.popBackStack()
    }
}
