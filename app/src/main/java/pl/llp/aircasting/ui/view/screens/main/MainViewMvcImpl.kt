package pl.llp.aircasting.ui.view.screens.main

import android.content.Intent
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
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.api.repositories.SessionsRepository
import pl.llp.aircasting.ui.view.screens.common.BaseViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.ui.view.screens.search.SearchFixedSessionsActivity
import pl.llp.aircasting.util.AnimatedLoader
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.adjustMenuVisibility

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
    private var mSearchIcon: ImageView? = null

    init {
        this.rootView = inflater.inflate(R.layout.activity_main, parent, false)
        this.loader = rootView?.loader
        mSettings = Settings(rootActivity.application)

        topAppBar = rootView?.findViewById(R.id.topAppBar)
        mReorderSessionsButton = rootView?.findViewById(R.id.reorder_sessions_button)
        mFinishedReorderingSessionsButton =
            rootView?.findViewById(R.id.finished_reordering_session_button)
        mSearchIcon = rootView?.findViewById(R.id.search_follow_icon)
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

        mSearchIcon?.setOnClickListener {
            val intent = Intent(rootActivity, SearchFixedSessionsActivity::class.java)
            rootActivity.startActivity(intent)
        }
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
                    mSettings?.getFollowedSessionsNumber()?.let { adjustMenuVisibility(rootActivity, true, it) }
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
                    mSearchIcon?.visibility = View.VISIBLE
                }
                R.id.navigation_lets_begin -> {
                    adjustMenuVisibility(rootActivity, false)
                    mNavController?.navigate(R.id.navigation_lets_begin)
                    mSearchIcon?.visibility = View.INVISIBLE
                }
                R.id.navigation_settings -> {
                    adjustMenuVisibility(rootActivity, false)
                    mNavController?.navigate(R.id.navigation_settings)
                    mSearchIcon?.visibility = View.INVISIBLE
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
