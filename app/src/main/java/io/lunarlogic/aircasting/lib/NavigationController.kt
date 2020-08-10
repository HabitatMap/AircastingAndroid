package io.lunarlogic.aircasting.lib

import androidx.navigation.NavController
import io.lunarlogic.aircasting.MobileNavigationDirections

class NavigationController {
    companion object {
        private lateinit var mNavController: NavController

        fun setup(navController: NavController) {
            mNavController = navController
        }

        fun goToDashboard(tabId: Int = 0) {
            val action = MobileNavigationDirections.actionGlobalDashboard(tabId)
            mNavController.navigate(action)
        }
    }
}
