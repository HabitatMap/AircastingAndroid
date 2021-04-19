package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.common.BaseController

class DashboardController(
    viewMvc: DashboardViewMvcImpl?
) : BaseController<DashboardViewMvcImpl>(viewMvc) {

    fun onCreate(tabId: Int?) {
        mViewMvc?.goToTab(tabId ?: 0)
    }
}
