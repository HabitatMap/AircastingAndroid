package io.lunarlogic.aircasting.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.events.ApplicationClosed
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.new_session.LoginActivity
import io.lunarlogic.aircasting.sensor.SessionManager
import org.greenrobot.eventbus.EventBus

class MainActivity : AppCompatActivity() {
    private var sessionManager: SessionManager? = null

    companion object {
        fun start(context: Context?) {
            context?.let{
                val intent = Intent(it, MainActivity::class.java)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard,
                R.id.navigation_lets_start,
                R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        val settings = Settings(this)

        if (settings.getAuthToken() == null) {
            LoginActivity.start(this)
            finish()
        } else {
            sessionManager = SessionManager(settings) // TODO: move to controller
        }

        sessionManager?.registerToEventBus()
    }

    override fun onDestroy() {
        super.onDestroy()

        sessionManager?.unregisterFromEventBus()
        EventBus.getDefault().post(ApplicationClosed())
    }
}
