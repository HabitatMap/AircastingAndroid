package io.lunarlogic.aircasting.lib

import androidx.navigation.NavController
import io.lunarlogic.aircasting.R

class NavigationController {
    companion object {
        private lateinit var mNavController: NavController

        fun setup(navController: NavController) {
            mNavController = navController
        }

        fun goToDashboard() {
            mNavController.navigate(R.id.navigation_dashboard)
        }
    }
}
