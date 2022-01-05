package pl.llp.aircasting.screens.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.AnimatedLoader
import pl.llp.aircasting.screens.common.BaseViewMvc
import kotlinx.android.synthetic.main.activity_main.view.*
import pl.llp.aircasting.screens.common.BaseObservableViewMvc

class MainViewMvcImpl: BaseObservableViewMvc<MainViewMvc.Listener>, MainViewMvc {
    private val rootActivity: AppCompatActivity
    private val loader: ImageView?
    private val finishedReorderingButton: Button?

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        rootActivity: AppCompatActivity): super() {
        this.rootView = inflater.inflate(R.layout.activity_main, parent, false)
        this.rootActivity = rootActivity

        this.loader = rootView?.loader
        this.finishedReorderingButton = rootView?.finished_reordering_session_button
        this.finishedReorderingButton?.setOnClickListener {
            onFinishReorderingSessionsClicked()
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
    }

    override fun showLoader() {
        AnimatedLoader(loader).start()
        loader?.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        loader?.visibility = View.GONE
    }

    override fun showAppBarMenu() {
        TODO("Not yet implemented")
    }

    override fun hideAppBarMenu() {
        TODO("Not yet implemented")
    }

    override fun showReorderingFinishedButton() {
        this.finishedReorderingButton?.visibility = View.VISIBLE
    }

    override fun hideReorderingFinishedButton() {
        this.finishedReorderingButton?.visibility = View.GONE
    }

    private fun onFinishReorderingSessionsClicked() {
        for (listener in listeners) {
            listener.onFinishedReorderingButtonClicked()
        }
    }
}
