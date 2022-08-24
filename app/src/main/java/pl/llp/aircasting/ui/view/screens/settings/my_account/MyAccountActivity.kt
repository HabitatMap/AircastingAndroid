package pl.llp.aircasting.ui.view.screens.settings.my_account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.local.LogoutService
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.util.extensions.setupAppBar
import javax.inject.Inject

class MyAccountActivity : BaseActivity() {

    private var controller: MyAccountController? = null

    @Inject
    lateinit var logoutService: LogoutService

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

        (application as AircastingApplication)
            .appComponent.inject(this)

        val view = MyAccountViewMvcImpl(this, layoutInflater, null)
        controller = MyAccountController(this, view, settings, logoutService)

        setContentView(view.rootView)
        setupAppBar(this, topAppBar)
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
