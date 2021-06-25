package pl.llp.aircasting.screens.settings.clear_sd_card.my_account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.lib.AppBar
import pl.llp.aircasting.lib.Settings
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

    override fun onDestroy() {
        super.onDestroy()
        AppBar.destroy()
    }
}
