package io.lunarlogic.aircasting.screens.new_session

import android.content.Context
import android.content.Intent
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
}
