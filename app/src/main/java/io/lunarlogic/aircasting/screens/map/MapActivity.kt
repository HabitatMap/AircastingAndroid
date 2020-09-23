package io.lunarlogic.aircasting.screens.map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.lib.AppBar

class MapActivity: AppCompatActivity() {
    private var controller: MapController? = null

    companion object {
        val SESSION_UUID_KEY = "SESSION_UUID"
        val SENSOR_NAME_KEY = "SENSOR_NAME"

        fun start(context: Context?, sessionUUID: String, sensorName: String?) {
            context?.let{
                val intent = Intent(it, MapActivity::class.java)
                intent.putExtra(SESSION_UUID_KEY, sessionUUID)
                intent.putExtra(SENSOR_NAME_KEY, sensorName)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionUUID: String = intent.extras?.get(SESSION_UUID_KEY) as String
        val sensorName: String? = intent.extras?.get(SENSOR_NAME_KEY) as String?

        val view = MapViewMvcImpl(layoutInflater, null, supportFragmentManager)
        controller = MapController(this, view, sessionUUID, sensorName)

        controller?.onCreate()

        setContentView(view.rootView)
        AppBar.setup(view.rootView, this)
    }

    override fun onDestroy() {
        super.onDestroy()

        controller?.onDestroy()
    }
}
