package pl.llp.aircasting.ui.view.screens.new_session

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.app_bar.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.NullError
import pl.llp.aircasting.util.extensions.goToFollowingTab
import pl.llp.aircasting.util.extensions.goToMobileActiveTab
import pl.llp.aircasting.util.extensions.setupAppBar
import javax.inject.Inject

class NewSessionActivity : BaseActivity() {

    private var controller: NewSessionController? = null

    @Inject
    lateinit var controllerFactory: NewSessionControllerFactory

    @Inject
    lateinit var errorHandler: ErrorHandler

    companion object {
        const val SESSION_TYPE_KEY = "sessionType"
        private lateinit var fixedLauncher: ActivityResultLauncher<Intent>
        private lateinit var mobileLauncher: ActivityResultLauncher<Intent>

        fun register(rootActivity: FragmentActivity?, sessionType: Session.Type) {

            rootActivity?.let {
                val launcher =
                    it.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                        if (it.resultCode == RESULT_OK) when (sessionType) {
                            Session.Type.FIXED -> rootActivity.goToFollowingTab()
                            Session.Type.MOBILE -> rootActivity.goToMobileActiveTab()
                        }
                    }

                when (sessionType) {
                    Session.Type.FIXED -> fixedLauncher = launcher
                    Session.Type.MOBILE -> mobileLauncher = launcher
                }
            }
        }

        fun start(rootActivity: FragmentActivity?, sessionType: Session.Type) {
            rootActivity?.let {
                val intent = Intent(it, NewSessionActivity::class.java)
                intent.putExtra(SESSION_TYPE_KEY, sessionType)

                when (sessionType) {
                    Session.Type.FIXED -> fixedLauncher.launch(intent)
                    Session.Type.MOBILE -> mobileLauncher.launch(intent)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionType = intent.extras?.get(SESSION_TYPE_KEY) as? Session.Type
        if (sessionType == null) {
            Log.e(TAG, "Session type was null")
            errorHandler.handle(NullError("Session type"))
            finish()
            return
        }

        (application as AircastingApplication).userDependentComponent?.inject(this)

        val view = NewSessionViewMvcImpl(layoutInflater, null)
        controller = controllerFactory.create(this, view, supportFragmentManager, sessionType)
        controller?.onCreate()

        setContentView(view.rootView)
        setupAppBar(this, topAppBar)
    }

    override fun onResume() {
        super.onResume()
        controller?.onResume()
    }

    override fun onStop() {
        super.onStop()
        controller?.onStop()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        controller?.onBackPressed()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        controller?.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        controller?.onActivityResult(requestCode, resultCode)
    }
}
