package pl.llp.aircasting.screens.sync.refreshed

import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.screens.common.BaseController

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
