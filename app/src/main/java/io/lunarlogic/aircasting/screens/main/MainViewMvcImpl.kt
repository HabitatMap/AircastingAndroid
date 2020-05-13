package io.lunarlogic.aircasting.screens.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseViewMvc

class MainViewMvcImpl: BaseViewMvc, MainViewMvc {
    private val rootActivity: AppCompatActivity

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        rootActivity: AppCompatActivity): super() {
        this.rootView = inflater.inflate(R.layout.activity_main, parent, false)
        this.rootActivity = rootActivity
    }

    fun setupBottomNavigationBar() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = rootActivity.findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard,
                R.id.navigation_lets_start,
                R.id.navigation_settings
            )
        )
        rootActivity.setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}