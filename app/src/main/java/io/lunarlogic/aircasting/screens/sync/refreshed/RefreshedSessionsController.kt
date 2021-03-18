package io.lunarlogic.aircasting.screens.sync.refreshed

import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.common.BaseController

class RefreshedSessionsController(
    private val mFragmentManager: FragmentManager,
    private var mViewMvc: RefreshedSessionsViewMvc?
) : BaseController(mView = mViewMvc) {
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
