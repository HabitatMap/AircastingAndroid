package io.lunarlogic.aircasting.screens.new_session

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import javax.inject.Inject

class LoginActivity: AppCompatActivity() {
    private var controller: LoginController? = null

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    companion object {
        fun start(contextActivity: AppCompatActivity?, animation: Boolean = false) {
            contextActivity?.let {
                val intent = Intent(it, LoginActivity::class.java)
                it.startActivity(intent)
                if (animation) {
                    it.overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out)
                }
            }
        }

        fun startAfterSignOut(context: Context?) {
            context?.let {
                val intent = Intent(it, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as AircastingApplication)
            .appComponent.inject(this)

        val view = LoginViewMvcImpl(layoutInflater, null, settings)
        controller = LoginController(this, view, settings, apiServiceFactory, supportFragmentManager)

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
