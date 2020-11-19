package io.lunarlogic.aircasting.screens.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.AnimatedLoader
import io.lunarlogic.aircasting.screens.common.BaseViewMvc
import kotlinx.android.synthetic.main.activity_main.view.*

class MainViewMvcImpl: BaseViewMvc, MainViewMvc {
    private val rootActivity: AppCompatActivity
    private val loader: ImageView?

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        rootActivity: AppCompatActivity): super() {
        this.rootView = inflater.inflate(R.layout.activity_main, parent, false)
        this.rootActivity = rootActivity

        this.loader = rootView?.loader
    }

    fun setupBottomNavigationBar(navController: NavController) {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

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

    override fun showLoader() {
        AnimatedLoader(loader).start()
        loader?.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        loader?.visibility = View.GONE
    }
}
