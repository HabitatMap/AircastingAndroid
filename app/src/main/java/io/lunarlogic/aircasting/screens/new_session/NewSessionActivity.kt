package io.lunarlogic.aircasting.screens.new_session

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class NewSessionActivity : AppCompatActivity() {
    private var mNewSessionController: NewSessionController? = null

    companion object {
        fun start(context: Context?) {
            context?.let{
                val intent = Intent(it, NewSessionActivity::class.java)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val newSessionView = NewSessionViewMvcImpl(layoutInflater, null)
        mNewSessionController = NewSessionController(this, newSessionView, supportFragmentManager)

        setContentView(newSessionView.rootView)
    }

    override fun onStart() {
        super.onStart()

        mNewSessionController!!.onStart()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mNewSessionController!!.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        println("Bluetooth onRequestPermissionsResult")
        when (requestCode) {
            AIRCASTING_PERMISSIONS_REQUEST_BLUETOOTH -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    println("Bluetooth permission enabled")
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    println("Bluetooth permission denied :(")
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("Bluetooth onActivityResult")
        when (requestCode) {
            AIRCASTING_REQUEST_BLUETOOTH_ENABLE -> {
                println("Bluetooth turn on! Yay!")
            }

            else -> {
                // Ignore all other requests.
            }
        }
    }
}
