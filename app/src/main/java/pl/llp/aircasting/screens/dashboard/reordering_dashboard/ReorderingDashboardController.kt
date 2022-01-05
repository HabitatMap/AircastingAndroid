package pl.llp.aircasting.screens.dashboard.reordering_dashboard

<<<<<<< HEAD
import pl.llp.aircasting.screens.common.BaseController
import pl.llp.aircasting.screens.dashboard.DashboardViewMvcImpl

class ReorderingDashboardController(
    private val viewMvc: DashboardViewMvcImpl?
) : BaseController<DashboardViewMvcImpl>(viewMvc) {

    fun onCreate(tabId: Int?) {
        viewMvc?.goToTab(tabId ?: 0)
    }
}
=======
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

>>>>>>> 6c94fa1c (first operational version)
