package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.BaseController

class DashboardController(
    private val viewMvc: DashboardViewMvcImpl?
) : BaseController<DashboardViewMvcImpl>(viewMvc) {
    private val mSessionRepository = SessionsRepository()

    fun onCreate(tabId: Int?) {
        DatabaseProvider.runQuery { scope ->
            if (mSessionRepository.mobileActiveSessionExists()) {
                DatabaseProvider.backToUIThread(scope) {
                    viewMvc?.goToTab(
                        DashboardPagerAdapter.tabIndexForSessionType(
                            Session.Type.MOBILE,
                            Session.Status.RECORDING
                        )
                    )
                }
            }
            else {
                DatabaseProvider.backToUIThread(scope) {
                    viewMvc?.goToTab(tabId ?: 0)
                }
            }
        }
    }
}
