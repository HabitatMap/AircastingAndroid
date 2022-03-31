package pl.llp.aircasting.screens.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.lib.AnimatedLoader
import pl.llp.aircasting.lib.NavigationController
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.lib.adjustMenuVisibility
import pl.llp.aircasting.screens.common.BaseViewMvc
import pl.llp.aircasting.screens.dashboard.SessionsTab

class MainViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    private val rootActivity: AppCompatActivity
) : BaseViewMvc(), MainViewMvc {
    private val loader: ImageView?
    private val mSessionRepository = SessionsRepository()
    private var mSettings: Settings? = null

    private var topAppBar: MaterialToolbar? = null
    private var mReorderSessionsButton: ImageView? = null
    private var mFinishedReorderingSessionsButton: Button? = null

    init {
        this.rootView = inflater.inflate(R.layout.activity_main, parent, false)
        this.loader = rootView?.loader
        mSettings = Settings(rootActivity.application)

        topAppBar = rootView?.findViewById(R.id.topAppBar)
        mReorderSessionsButton = rootView?.findViewById(R.id.reorder_sessions_button)
        mFinishedReorderingSessionsButton =
            rootView?.findViewById(R.id.finished_reordering_session_button)
    }

    fun appBarSetup() {
        rootActivity.setSupportActionBar(topAppBar)
        topAppBar?.setNavigationOnClickListener {
            rootActivity.onBackPressed()
        }

        mReorderSessionsButton?.setOnClickListener {
            NavigationController.goToReorderingDashboard()
            showReorderSessionsButton()
        }

        mFinishedReorderingSessionsButton?.setOnClickListener {
            NavigationController.goToDashboard(SessionsTab.FOLLOWING.value)
            showFinishedReorderingSessionsButtonClicked()
        }
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
                    mSettings?.getFollowedSessionsNumber()
                        ?.let { adjustMenuVisibility(rootActivity, true, it) }
                    DatabaseProvider.runQuery { scope ->
                        val isMobileActiveSessionExists =
                            mSessionRepository.mobileActiveSessionExists()

                        DatabaseProvider.backToUIThread(scope) {
                            if (isMobileActiveSessionExists) NavigationController.goToDashboard(
                                SessionsTab.MOBILE_ACTIVE.value
                            ) else NavigationController.goToDashboard(
                                SessionsTab.FOLLOWING.value
                            )
                        }

                    }
                }
                R.id.navigation_lets_start -> {
                    adjustMenuVisibility(rootActivity, false)
                    NavigationController.goToLetsStart()
                }
                R.id.navigation_settings -> {
                    adjustMenuVisibility(rootActivity, false)
                    NavigationController.goToSettings()
                }
            }
            true
        }
    }

    private fun showReorderSessionsButton() {
        mFinishedReorderingSessionsButton?.visibility = View.VISIBLE
        mReorderSessionsButton?.visibility = View.INVISIBLE
    }

    fun showFinishedReorderingSessionsButtonClicked() {
        mFinishedReorderingSessionsButton?.visibility = View.INVISIBLE
        mReorderSessionsButton?.visibility = View.VISIBLE
    }

    override fun showLoader() {
        AnimatedLoader(loader).start()
        loader?.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        loader?.visibility = View.GONE
    }
}
