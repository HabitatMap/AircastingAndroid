package io.lunarlogic.aircasting.screens.dashboard


class DashboardController(
    private var mView: DashboardViewMvc?
) {

    fun onCreate(tabId: Int?) {
        mView?.goToTab(tabId ?: 0)
    }

    fun onDestroy() {
        mView = null
    }
}
