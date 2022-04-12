package pl.llp.aircasting.screens.main

import android.os.Handler
import android.os.Looper
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
import pl.llp.aircasting.MobileNavigationDirections
import pl.llp.aircasting.R
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.lib.AnimatedLoader
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
    private var mNavController: NavController? = null
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

    fun setupNavController(navController: NavController) {
        mNavController = navController
    }

    fun appBarSetup() {
        rootActivity.setSupportActionBar(topAppBar)
        topAppBar?.setNavigationOnClickListener {
            rootActivity.onBackPressed()
        }

        mReorderSessionsButton?.setOnClickListener {
            mNavController?.navigate(R.id.navigation_reordering_dashboard)
            showReorderSessionsButton()
        }

        mFinishedReorderingSessionsButton?.setOnClickListener {
            val action = MobileNavigationDirections.actionGlobalDashboard(SessionsTab.FOLLOWING.value)
            mNavController?.navigate(action)
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
                            if (isMobileActiveSessionExists) {
                                val action = MobileNavigationDirections.actionGlobalDashboard(SessionsTab.MOBILE_ACTIVE.value)
                                mNavController?.navigate(action)} else {
                                val action = MobileNavigationDirections.actionGlobalDashboard(SessionsTab.FOLLOWING.value)
                                mNavController?.navigate(action)
                            }
                        }

                    }
                }
                R.id.navigation_lets_start -> {
                    adjustMenuVisibility(rootActivity, false)
                    mNavController?.navigate(R.id.navigation_lets_start)
                }
                R.id.navigation_settings -> {
                    adjustMenuVisibility(rootActivity, false)
                    mNavController?.navigate(R.id.navigation_settings)
                }
            }
            true
        }
    }

    private fun showReorderSessionsButton() {
        mFinishedReorderingSessionsButton?.visibility = View.VISIBLE
        mReorderSessionsButton?.visibility = View.INVISIBLE
    }

    private fun showFinishedReorderingSessionsButtonClicked() {
        mFinishedReorderingSessionsButton?.visibility = View.INVISIBLE
        mReorderSessionsButton?.visibility = View.VISIBLE
    }

    override fun showLoader() {
        AnimatedLoader(loader).start()
        loader?.visibility = View.VISIBLE
    }

    // Considering delay as the last resort sync data is being bound into RecyclerView after some time.
    // The performance and the binding section can be improved.
    override fun hideLoader() {
        Handler(Looper.getMainLooper()).postDelayed({
            AnimatedLoader(loader).stop()
            loader?.visibility = View.GONE
        }, 10000)
    }
}
