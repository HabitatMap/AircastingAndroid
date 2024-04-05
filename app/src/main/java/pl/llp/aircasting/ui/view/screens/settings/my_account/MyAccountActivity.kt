package pl.llp.aircasting.ui.view.screens.settings.my_account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_myaccount.view.*
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.local.LogoutService
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.ui.view.screens.dashboard.ConfirmDangerActionDialog
import pl.llp.aircasting.ui.view.screens.dashboard.ConfirmDangerCodeActionDialog
import pl.llp.aircasting.util.extensions.setupAppBar
import javax.inject.Inject
import android.util.Log


class MyAccountActivity : BaseActivity() {

    private lateinit var rootView: View

    @Inject
    lateinit var logoutService: LogoutService

    @Inject
    lateinit var viewModel: MyAccountViewModel

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
            .userDependentComponent?.inject(this)

        rootView = layoutInflater.inflate(R.layout.activity_myaccount, null, false)

        setupLayout()
        setContentView(rootView)
        setupAppBar(this, topAppBar)
    }

    private fun setupLayout() {
        rootView.apply {
            sign_out_button.setOnClickListener { viewModel.logout() }
            delete_account_button.setOnClickListener { showAreYouSureDialog() }
            header.text = getString(R.string.my_account_info).format(viewModel.userName)
        }
    }

    private fun showAreYouSureDialog() {
        Log.e("Marta", "marta");
        ConfirmDangerActionDialog(
            supportFragmentManager,
            okCallback = {
                viewModel.deleteAccountSendEmail()
                showDeleteConfirmationCodeDialog()
            }
        ).show()
    }

    private fun showDeleteConfirmationCodeDialog() {
        ConfirmDangerCodeActionDialog(
            supportFragmentManager,
            viewModel.userName,
            okCallback = { viewModel.deleteAccountConfirmCode(it) }
        ).show()
    }
}
