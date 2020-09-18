package io.lunarlogic.aircasting.screens.map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MapActivity: AppCompatActivity() {
    private var controller: MapController? = null

    companion object {
        fun start(context: Context?) {
            context?.let{
                val intent = Intent(it, MapActivity::class.java)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = MapViewMvcImpl(layoutInflater, null)
        controller = MapController(this, view)

        controller?.onCreate()

        setContentView(view.rootView)
    }

    override fun onDestroy() {
        super.onDestroy()

        controller?.onDestroy()
    }
}
