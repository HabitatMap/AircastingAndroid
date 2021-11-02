package pl.llp.aircasting.screens.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.libraries.places.api.Places
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.BuildConfig
import pl.llp.aircasting.R
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.exceptions.AircastingUncaughtExceptionHandler
import pl.llp.aircasting.lib.*
import pl.llp.aircasting.location.LocationHelper
import pl.llp.aircasting.networking.services.ApiServiceFactory
import javax.inject.Inject

class MainActivity: AppCompatActivity() {
    private var controller: MainController? = null
    private var view: MainViewMvcImpl? = null

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    companion object {
        fun start(context: Context?) {
            context?.let{
                val intent = Intent(it, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as AircastingApplication)
            .appComponent.inject(this)

        // subscribing to custom uncaught exception handler to handle crash
        Thread.setDefaultUncaughtExceptionHandler(AircastingUncaughtExceptionHandler(settings));

        DatabaseProvider.setup(applicationContext)
        LocationHelper.setup(applicationContext)
        DateConverter.setup(settings)
        Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)

        view = MainViewMvcImpl(layoutInflater, null, this)
        controller = MainController(this, view!!, settings, apiServiceFactory)

        controller?.onCreate()

        setContentView(view?.rootView)
        AppBar.setup(view?.rootView, this)

        val navController = findNavController(R.id.nav_host_fragment)
        NavigationController.setup(navController)
        view?.setupBottomNavigationBar(navController)
    }

    override fun onResume() {
        super.onResume()
        AppBar.setup(view?.rootView, this)
    }

    override fun onDestroy() {
        super.onDestroy()

        AppBar.destroy()
        controller?.onDestroy()
        LocationHelper.stop()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        controller?.onRequestPermissionsResult(requestCode, grantResults)
    }
}
