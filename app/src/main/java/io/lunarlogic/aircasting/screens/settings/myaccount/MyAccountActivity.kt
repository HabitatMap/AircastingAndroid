package io.lunarlogic.aircasting.screens.settings.myaccount

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.screens.new_session.LoginActivity
import io.lunarlogic.aircasting.screens.new_session.LoginController
import io.lunarlogic.aircasting.screens.new_session.LoginViewMvcImpl

class MyAccountActivity : AppCompatActivity() {

    private var controller: MyAccountController? = null

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
        controller = MyAccountController(view)

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