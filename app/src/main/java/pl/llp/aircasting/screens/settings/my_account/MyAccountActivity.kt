package pl.llp.aircasting.screens.settings.my_account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.common.BaseActivity

class MyAccountActivity : BaseActivity() {

    private var controller: MyAccountController? = null

    companion object {
        fun start(context: Context?) {
            context?.let {
                val intent = Intent(it, MyAccountActivity::class.java)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = MyAccountViewMvcImpl(this, layoutInflater, null)
        controller = MyAccountController(this, view, settings)

        setContentView(view.rootView)
        setupAppBar()
    }

    private fun setupAppBar() {
        setSupportActionBar(topAppBar)
        topAppBar?.findViewById<ConstraintLayout>(R.id.reorder_buttons_group)?.visibility =
            View.INVISIBLE
        topAppBar?.setNavigationOnClickListener {
            onBackPressed()
        }
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
