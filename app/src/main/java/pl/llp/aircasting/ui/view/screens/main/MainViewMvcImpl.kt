package pl.llp.aircasting.ui.view.screens.main

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.view.*
import pl.llp.aircasting.MobileNavigationDirections
import pl.llp.aircasting.R
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.ui.view.common.BaseViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.ui.view.screens.search.SearchFixedSessionActivity
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.extensions.*

class MainViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    private val rootActivity: AppCompatActivity
) : BaseViewMvc(), MainViewMvc {
    private val loader: ImageView?
    private val mSessionRepository = SessionsRepository()
    private var mSettings: Settings? = null

    private lateinit var topAppBar: MaterialToolbar
    private lateinit var mNavController: NavController
    private lateinit var mReorderLayout: ConstraintLayout
    private lateinit var mFinishReorderLayout: ConstraintLayout

    private lateinit var mReorderSessionsButton: ImageView
    private lateinit var mFinishedReorderingSessionsButton: Button
    private lateinit var mSearchIcon: ImageView

    init {
        this.rootView = inflater.inflate(R.layout.activity_main, parent, false)
        this.loader = rootView?.loader
        mSettings = Settings(rootActivity.application)

        rootView?.apply {
            topAppBar = findViewById(R.id.topAppBar)

            mReorderLayout = findViewById(R.id.reorder_buttons_group)
            mFinishReorderLayout = findViewById(R.id.finishReorderLayout)

            mReorderSessionsButton = findViewById(R.id.reorderButton)
            mSearchIcon = findViewById(R.id.search_follow_icon)

            mFinishedReorderingSessionsButton =
                findViewById(R.id.finished_reordering_session_button)
        }
    }

    fun setupNavController(navController: NavController) {
        mNavController = navController
    }

    fun appBarSetup() {
        setupTopAppBar()

        mReorderSessionsButton.setOnClickListener { onReorderSessionsClicked() }
        mFinishedReorderingSessionsButton.setOnClickListener { showFinishedReorderingSessionsButtonClicked() }
        mSearchIcon.setOnClickListener { onSearchIconClicked() }
    }

    private fun setupTopAppBar() {
        topAppBar.let {
            rootActivity.setSupportActionBar(it)
            it.setNavigationOnClickListener {
                rootActivity.onBackPressed()
            }
        }
    }

    private fun onReorderSessionsClicked() {
        mNavController.navigate(R.id.navigation_reordering_dashboard)
        mReorderLayout.gone()
        mFinishReorderLayout.visible()
    }

    private fun onSearchIconClicked() {
        val intent = Intent(rootActivity, SearchFixedSessionActivity::class.java)
        rootActivity.startActivity(intent)
    }

    private fun showFinishedReorderingSessionsButtonClicked() {
        val action = MobileNavigationDirections.actionGlobalDashboard(SessionsTab.FOLLOWING.value)
        mNavController.navigate(action)

        mFinishReorderLayout.gone()
        mReorderLayout.visible()
    }

    fun setupBottomNavigationBar(navController: NavController) {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard,
                R.id.navigation_lets_begin,
                R.id.navigation_settings
            )
        )
        rootActivity.setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    mSettings?.getFollowedSessionsNumber()
                        ?.let { rootActivity.adjustMenuVisibility(true, it) }

                    navigateToAppropriateTab()
                }
                R.id.navigation_lets_begin -> mNavController.navigate(R.id.navigation_lets_begin)
                R.id.navigation_settings -> mNavController.navigate(R.id.navigation_settings)
            }
            true
        }
    }

    private fun navigateToAppropriateTab() {
        DatabaseProvider.runQuery { scope ->
            val isMobileActiveSessionExists = mSessionRepository.mobileActiveSessionExists()

            DatabaseProvider.backToUIThread(scope) {
                if (isMobileActiveSessionExists) goToMobileActiveTab() else goToFollowingTab()
            }
        }
    }

    private fun goToMobileActiveTab() {
        val action =
            MobileNavigationDirections.actionGlobalDashboard(SessionsTab.MOBILE_ACTIVE.value)
        mNavController.navigate(action)
    }

    private fun goToFollowingTab() {
        val action = MobileNavigationDirections.actionGlobalDashboard(SessionsTab.FOLLOWING.value)
        mNavController.navigate(action)
    }

    override fun showLoader() {
        loader?.startAnimation()
    }

    // TODO: The recyclerView needs to be improved later.
    override fun hideLoader() {
        Handler(Looper.getMainLooper()).postDelayed({
            loader?.stopAnimation()
        }, 10000)
    }
}
