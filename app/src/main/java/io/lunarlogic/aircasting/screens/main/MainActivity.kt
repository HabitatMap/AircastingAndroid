package io.lunarlogic.aircasting.screens.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.location.LocationHelper

class MainActivity : AppCompatActivity() {
    private var controller: MainController? = null

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
        DatabaseProvider.setup(applicationContext)
        LocationHelper.setup(applicationContext)

        val view = MainViewMvcImpl(layoutInflater, null, this)
        controller = MainController(this, view)

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
