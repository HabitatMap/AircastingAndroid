package io.lunarlogic.aircasting.screens.settings.myaccount

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.lib.Settings
import javax.inject.Inject

class MyAccountActivity : AppCompatActivity() {

    private var controller: MyAccountController? = null

    @Inject
    lateinit var settings: Settings  //Todo: this settings a bit random for now <?>

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

        // todo: do i need AirCastingApplication injection here??

        val view = MyAccountViewMvcImpl(layoutInflater, null)
        controller = MyAccountController(this, view, settings)

        setContentView(view.rootView)
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