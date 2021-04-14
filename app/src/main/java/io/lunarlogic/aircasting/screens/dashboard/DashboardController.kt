package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.models.Session


class DashboardController(
    private val mView: DashboardViewMvc
) {
    private val mSessionRepository = SessionsRepository() //TODO: it works fine, just asking myself- if thats good place to init SessionsRepository, runQuery{} etc.

    fun onCreate(tabId: Int?) {
        DatabaseProvider.runQuery {
            if (mSessionRepository.mobileActiveSessionExists()) mView.goToTab(DashboardPagerAdapter.tabIndexForSessionType(Session.Type.MOBILE, Session.Status.RECORDING))
            else mView.goToTab(tabId ?: 0)
        }
    }
}
