package io.lunarlogic.aircasting.screens.new_session

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.Settings
import javax.inject.Inject

class LoginActivity: AppCompatActivity() {
    private var controller: LoginController? = null

    @Inject
    lateinit var settings: Settings

    companion object {
        fun start(context: Context?) {
            context?.let{
                val intent = Intent(it, LoginActivity::class.java)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as AircastingApplication)
            .appComponent.inject(this)

        val view = LoginViewMvcImpl(layoutInflater, null)
        controller = LoginController(this, view, settings)

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

    override fun onBackPressed() {
        // Doing nothing here, we need it to prevent going back after log out
    }
}
