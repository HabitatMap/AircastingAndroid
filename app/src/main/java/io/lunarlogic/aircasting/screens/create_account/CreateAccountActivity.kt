package io.lunarlogic.aircasting.screens.create_account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import javax.inject.Inject

/**
 * Created by Maria Turnau on 02/09/2020.
 */

class CreateAccountActivity: AppCompatActivity() {
    private var controller: CreateAccountController? = null

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    companion object {
        fun start(contextActivity: AppCompatActivity?) {
            contextActivity?.let{
                val intent = Intent(it, CreateAccountActivity::class.java)
                it.startActivity(intent)
                it.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as AircastingApplication)
            .appComponent.inject(this)

        val view = CreateAccountViewMvcImpl(layoutInflater, null)
        controller = CreateAccountController(this, view, settings, apiServiceFactory)

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
