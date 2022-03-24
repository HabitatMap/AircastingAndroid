package pl.llp.aircasting.screens.main

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
import kotlinx.android.synthetic.main.activity_main.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.lib.AnimatedLoader
import pl.llp.aircasting.lib.NavigationController
import pl.llp.aircasting.screens.common.BaseViewMvc

class MainViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    private val rootActivity: AppCompatActivity
) : BaseViewMvc(), MainViewMvc {
    private val loader: ImageView?
    private val mSessionRepository = SessionsRepository()

    init {
        this.rootView = inflater.inflate(R.layout.activity_main, parent, false)
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

        navView.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.navigation_dashboard -> {

                    DatabaseProvider.runQuery { scope ->
                        val isMobileActiveSessionExists =
                            mSessionRepository.mobileActiveSessionExists()

                        DatabaseProvider.backToUIThread(scope) {
                            if (isMobileActiveSessionExists) NavigationController.goToDashboard(1) else NavigationController.goToDashboard(
                                0
                            )
                        }

                    }
                }

                R.id.navigation_lets_start -> NavigationController.goToLetsStart()
                R.id.navigation_settings -> NavigationController.goToSettings()
            }
            true
        }
    }

    override fun showLoader() {
        AnimatedLoader(loader).start()
        loader?.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        loader?.visibility = View.GONE
    }
}
