package pl.llp.aircasting.lib

import androidx.navigation.NavController
import pl.llp.aircasting.MobileNavigationDirections
import pl.llp.aircasting.R

class NavigationController {
    companion object {
        private lateinit var mNavController: NavController

        fun setup(navController: NavController) {
            mNavController = navController
        }

        fun goToDashboard(tabId: Int) {
            val action = MobileNavigationDirections.actionGlobalDashboard(tabId)
            mNavController.navigate(action)
        }

        fun goToLetsStart() {
            mNavController.navigate(R.id.navigation_lets_start)
        }

        fun goToReorderingDashboard() {
            mNavController.navigate(R.id.navigation_reordering_dashboard)
        }

        fun goToSettings() {
            mNavController.navigate(R.id.navigation_settings)
        }
    }
}