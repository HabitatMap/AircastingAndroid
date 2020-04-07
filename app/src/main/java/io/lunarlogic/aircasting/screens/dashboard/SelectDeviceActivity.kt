package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.R

class SelectDeviceActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context?) {
            context?.let{
                val intent = Intent(it, SelectDeviceActivity::class.java)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_device)
    }
}
