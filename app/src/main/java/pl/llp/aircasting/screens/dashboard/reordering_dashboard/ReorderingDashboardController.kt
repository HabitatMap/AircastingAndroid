package pl.llp.aircasting.screens.dashboard.reordering_dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.screens.common.BaseController

class ReorderingDashboardController(
    private val viewMvc: ReorderingDashboardViewMvcImpl?
) : BaseController<ReorderingDashboardViewMvcImpl>(viewMvc) {

    fun onCreate(tabId: Int?) {
        DatabaseProvider.runQuery { scope ->
            DatabaseProvider.backToUIThread(scope) {
                viewMvc?.goToTab(tabId ?: 0)
            }
        }
    }
}

