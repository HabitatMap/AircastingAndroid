package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.common.BaseController

class DashboardController(
    private var mView: DashboardViewMvc?
) : BaseController(mView = mView){

    fun onCreate(tabId: Int?) {
        mView?.goToTab(tabId ?: 0)
    }
}
