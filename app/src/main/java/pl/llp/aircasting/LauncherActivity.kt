package pl.llp.aircasting

import android.os.Bundle
import android.transition.Fade
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.ui.view.screens.onboarding.OnboardingActivity
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.LogoutEvent
import javax.inject.Inject

class LauncherActivity : AppCompatActivity() {
    @Inject
    lateinit var mSettings: Settings
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        setTheme(R.style.Theme_Aircasting)
        super.onCreate(savedInstanceState)

        (application as AircastingApplication)
            .appComponent.inject(this)
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

            exitTransition = Fade()
        }

        val logout = EventBus.getDefault().getStickyEvent(LogoutEvent::class.java)

        if (!mSettings.onboardingDisplayed() && mSettings.getAuthToken() == null) {
            showOnboardingScreen()
        } else if (mSettings.getAuthToken() == null || logout?.inProgress == true) {
            showLoginScreen()
        } else {
            showDashboard()
        }

        finishAfterTransition()
    }

    private fun showDashboard() {
        MainActivity.start(this)
    }

    private fun showLoginScreen() {
        LoginActivity.start(this)
    }

    private fun showOnboardingScreen() {
        OnboardingActivity.start(this)
    }
}