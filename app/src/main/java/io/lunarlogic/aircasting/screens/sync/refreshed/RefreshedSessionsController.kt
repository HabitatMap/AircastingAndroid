package io.lunarlogic.aircasting.screens.sync.refreshed

import androidx.fragment.app.FragmentManager

class RefreshedSessionsController(
    private val mFragmentManager: FragmentManager,
    private val mViewMvc: RefreshedSessionsViewMvc
) {
    fun registerListener(listener: RefreshedSessionsViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: RefreshedSessionsViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }

    fun onBackPressed() {
        mFragmentManager.popBackStack()
    }
}
