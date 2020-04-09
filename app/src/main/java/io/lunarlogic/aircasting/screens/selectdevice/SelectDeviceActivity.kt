package io.lunarlogic.aircasting.screens.selectdevice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SelectDeviceActivity : AppCompatActivity() {
    private var mSelectDeviceController: SelectDeviceController? = null

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

        val selectDeviceView =
            SelectDeviceViewMvcImpl(
                layoutInflater,
                null
            )
        mSelectDeviceController =
            SelectDeviceController(
                this,
                selectDeviceView
            )
        setContentView(selectDeviceView.rootView)
    }

    override fun onStart() {
        super.onStart()
        mSelectDeviceController!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mSelectDeviceController!!.onStop()
    }
}
