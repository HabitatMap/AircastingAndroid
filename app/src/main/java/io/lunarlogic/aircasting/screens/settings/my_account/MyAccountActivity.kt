package io.lunarlogic.aircasting.screens.settings.my_account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.AppBar
import io.lunarlogic.aircasting.lib.Settings
import javax.inject.Inject

class MyAccountActivity : AppCompatActivity() {

    private var controller: MyAccountController? = null

    @Inject
    lateinit var settings: Settings

    companion object{
        fun start(context: Context?) {
            context?.let{
                val intent = Intent(it, MyAccountActivity::class.java)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as AircastingApplication)
            .appComponent.inject(this)

        val view = MyAccountViewMvcImpl(this, layoutInflater, null)
        controller = MyAccountController(this, view, settings)

        setContentView(view.rootView)
        AppBar.setup(view.rootView, this)

    }

    override fun onStart() {
        super.onStart()
        controller?.onStart()
    }

    override fun onStop() {
        super.onStop()
        controller?.onStop()
    }

}
