package pl.llp.aircasting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        super.onCreate(savedInstanceState)

        (application as AircastingApplication)
            .appComponent.inject(this)

        val logout = EventBus.getDefault().getStickyEvent(LogoutEvent::class.java)

        if (!mSettings.onboardingDisplayed() && mSettings.getAuthToken() == null) {
            showOnboardingScreen()
        } else if (mSettings.getAuthToken() == null || logout?.inProgress == true) {
            showLoginScreen()
        } else {
            showDashboard()
        }
        finish()
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