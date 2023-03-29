package pl.llp.aircasting.ui.view.screens.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapsInitializer.Renderer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.libraries.places.api.Places
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.BuildConfig
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardPagerAdapter.Companion.FOLLOWING_TAB_INDEX
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.TemperatureConverter
import pl.llp.aircasting.util.exceptions.AircastingUncaughtExceptionHandler
import pl.llp.aircasting.util.extensions.goToFollowingTab
import pl.llp.aircasting.util.extensions.goToMobileActiveTab
import pl.llp.aircasting.util.extensions.goToMobileDormantTab
import pl.llp.aircasting.util.helpers.location.LocationHelper
import javax.inject.Inject

class MainActivity : BaseActivity(), OnMapsSdkInitializedCallback {
    @Inject
    lateinit var controllerFactory: MainControllerFactory
    var controller: MainController? = null
    private var view: MainViewMvcImpl? = null

    companion object {
        const val NAVIGATE_TO_TAB = "navigate_to_tab"
        fun start(fromContext: Context?) {
            fromContext?.let {
                val intent = Intent(it, MainActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                it.startActivity(intent)
            }
        }
        fun navigate(fromContext: Context?, tabToNavigateTo: Int = FOLLOWING_TAB_INDEX) {
            fromContext?.let {
                val intent = Intent(it, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra(NAVIGATE_TO_TAB, tabToNavigateTo)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as AircastingApplication)
            .userDependentComponent?.inject(this)

        // subscribing to custom uncaught exception handler to handle crash
        Thread.setDefaultUncaughtExceptionHandler(AircastingUncaughtExceptionHandler(settings))

        LocationHelper.setup(applicationContext)
        DateConverter.setup(settings)
        TemperatureConverter.setup(settings)

        //New map renderer
        MapsInitializer.initialize(applicationContext, null, this)
        Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)

        view = MainViewMvcImpl(layoutInflater, null, this)
        controller = controllerFactory.create(this)

        controller?.onCreate()
        setContentView(view?.rootView)
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val tab = intent?.getIntExtra(NAVIGATE_TO_TAB, FOLLOWING_TAB_INDEX) ?: FOLLOWING_TAB_INDEX
        when (SessionsTab.fromInt(tab)) {
            SessionsTab.MOBILE_DORMANT -> goToMobileDormantTab()
            SessionsTab.MOBILE_ACTIVE -> goToMobileActiveTab()
            else -> goToFollowingTab()
        }
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
}
