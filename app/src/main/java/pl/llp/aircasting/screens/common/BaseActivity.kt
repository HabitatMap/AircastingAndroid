package pl.llp.aircasting.screens.common

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.lib.Settings
import javax.inject.Inject

open class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as AircastingApplication)
            .appComponent.inject(this)
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
