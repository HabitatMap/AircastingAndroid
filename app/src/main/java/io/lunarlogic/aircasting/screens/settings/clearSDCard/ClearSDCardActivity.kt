package io.lunarlogic.aircasting.screens.settings.clearSDCard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ClearSDCardActivity : AppCompatActivity() {
    private var controller: ClearSDCardActivity? = null

    companion object{
        fun start(context: Context?) {
            context?.let{
                val intent = Intent(it, ClearSDCardActivity::class.java)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //todo: inject activity in app component needed for sure
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
