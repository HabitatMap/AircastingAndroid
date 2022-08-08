package pl.llp.aircasting.ui.view.screens.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatActivity
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.ui.view.common.BaseActivity
import javax.inject.Inject

class LoginActivity : BaseActivity() {
    private var controller: LoginController? = null

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    companion object {
        const val FROM_ONBOARDING_KEY = "fromOnboarding"

        fun start(
            contextActivity: AppCompatActivity?,
            animation: Boolean = false,
            fromOnboarding: Boolean? = false
        ) {
            contextActivity?.let {
                val intent = Intent(it, LoginActivity::class.java)
                intent.putExtra(FROM_ONBOARDING_KEY, fromOnboarding)
                it.startActivity(intent)
                if (animation) {
                    it.overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out)
                }
            }
        }

        fun startAfterSignOut(context: Context?) {
            context?.let {
                val intent = Intent(it, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fromOnboarding = intent.extras?.get(FROM_ONBOARDING_KEY) as Boolean?

        (application as AircastingApplication)
            .appComponent.inject(this)

        val view = LoginViewMvcImpl(layoutInflater, null, settings, fromOnboarding)
        controller =
            LoginController(this, view, settings, apiServiceFactory, supportFragmentManager)

        setContentView(view.rootView)
    }

    override fun onStart() {
        super.onStart()
        controller!!.onStart()
    }


    override fun onStop() {
        super.onStop()
        controller!!.onStop()
    }
}
