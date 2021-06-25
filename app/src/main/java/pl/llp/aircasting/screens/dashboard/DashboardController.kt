package pl.llp.aircasting.screens.dashboard

import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.common.BaseController

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
