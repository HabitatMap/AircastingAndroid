package io.lunarlogic.aircasting.screens.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.BuildConfig
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.lib.Settings
import javax.inject.Inject

class MainActivity: AppCompatActivity() {
    private var controller: MainController? = null

    @Inject
    lateinit var settings: Settings

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

        (application as AircastingApplication)
            .appComponent.inject(this)

        DatabaseProvider.setup(applicationContext)
        LocationHelper.setup(applicationContext)
        Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)

        val view = MainViewMvcImpl(layoutInflater, null, this)
        controller = MainController(this, view, settings)

        controller?.onCreate()

        setContentView(view.rootView)
        view.setupBottomNavigationBar()
    }

    override fun onDestroy() {
        super.onDestroy()

        controller?.onDestroy()
        LocationHelper.stop()
    }
}
