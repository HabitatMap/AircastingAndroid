package io.lunarlogic.aircasting.screens.dashboard


class DashboardController(
    private val mView: DashboardViewMvc
) {

    fun onCreate(tabId: Int?) {
        mView.goToTab(tabId ?: 0)
    }
}
