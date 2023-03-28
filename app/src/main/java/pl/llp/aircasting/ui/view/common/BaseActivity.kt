package pl.llp.aircasting.ui.view.common

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.util.Settings
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as AircastingApplication).apply {
            appComponent.inject(this@BaseActivity)
            userDependentComponent?.inject(this@BaseActivity)
        }
    }

    override fun onResume() {
        super.onResume()
        if (settings.isKeepScreenOnEnabled()) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
