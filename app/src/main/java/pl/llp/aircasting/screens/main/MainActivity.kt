package pl.llp.aircasting.screens.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapsInitializer.Renderer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.libraries.places.api.Places
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.BuildConfig
import pl.llp.aircasting.R
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.exceptions.AircastingUncaughtExceptionHandler
import pl.llp.aircasting.lib.DateConverter
import pl.llp.aircasting.lib.TemperatureConverter
import pl.llp.aircasting.lib.isIgnoringBatteryOptimizations
import pl.llp.aircasting.lib.isSDKVersionBiggerThanM
import pl.llp.aircasting.location.LocationHelper
import pl.llp.aircasting.networking.services.ApiServiceFactory
import pl.llp.aircasting.screens.common.BaseActivity
import javax.inject.Inject

class MainActivity : BaseActivity(), OnMapsSdkInitializedCallback {
    private var controller: MainController? = null
    private var view: MainViewMvcImpl? = null
    private var isBackButtonPressed = false

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    private lateinit var mNavController: NavController

    companion object {
        fun start(context: Context?) {
            context?.let {
                val intent = Intent(it, MainActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as AircastingApplication)
            .appComponent.inject(this)

        // subscribing to custom uncaught exception handler to handle crash
        Thread.setDefaultUncaughtExceptionHandler(AircastingUncaughtExceptionHandler(settings))

        DatabaseProvider.setup(applicationContext)
        LocationHelper.setup(applicationContext)
        DateConverter.setup(settings)
        TemperatureConverter.setup(settings)

        //New map renderer
        MapsInitializer.initialize(applicationContext, Renderer.LATEST, this)
        Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)

        view = MainViewMvcImpl(layoutInflater, null, this)
        controller =
            MainController(this, view!!, settings, supportFragmentManager, apiServiceFactory)

        controller?.onCreate()

        setContentView(view?.rootView)
        view?.appBarSetup()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        mNavController = navHostFragment.navController

        view?.setupNavController(mNavController)
        view?.setupBottomNavigationBar(mNavController)
    }

    override fun onResume() {
        super.onResume()
        controller?.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()

        controller?.onDestroy()
        LocationHelper.stop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        controller?.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onMapsSdkInitialized(renderer: Renderer) {
        when (renderer) {
            Renderer.LATEST -> Log.d("MapsDemo", "The latest version of the renderer is used.")
            Renderer.LEGACY -> Log.d("MapsDemo", "The legacy version of the renderer is used.")
        }
    }

    override fun onBackPressed() {
        if (isBackButtonPressed) {
            super.onBackPressed()
            return
        }
        isBackButtonPressed = true

        // show explanation about background task and let the user disable the battery optimization
        if (isSDKVersionBiggerThanM() && !isIgnoringBatteryOptimizations(applicationContext)) controller?.showBatteryOptimizationHelperDialog()

        Handler(Looper.getMainLooper()).postDelayed({
            isBackButtonPressed = false
        }, 2000)
    }
}
