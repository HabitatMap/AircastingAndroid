package pl.llp.aircasting.ui.view.screens.sync

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.app_bar.topAppBar
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.util.extensions.setupAppBar
import javax.inject.Inject

class SyncActivity : BaseActivity() {
    private var controller: SyncController? = null

    @Inject
    lateinit var controllerFactory: SyncControllerFactory

    companion object {
        private const val UUID_EXTRA_KEY = "sessionUuid"
        private var launcher: ActivityResultLauncher<Intent>? = null

        fun register(rootActivity: FragmentActivity?, onFinish: (() -> Unit)? = null) {
            launcher =
                rootActivity?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    onFinish?.invoke()
                }
            // TODO: we should be using "Inline functions" for this onFinish() invoke as it is good for the performance!
            // More info: https://stackoverflow.com/questions/44471284/when-to-use-an-inline-function-in-kotlin
        }

        fun start(rootActivity: FragmentActivity?, sessionUuid: String? = null) {
            rootActivity ?: return

            val intent = Intent(rootActivity, SyncActivity::class.java)
                .putExtra(UUID_EXTRA_KEY, sessionUuid)
            launcher?.launch(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as AircastingApplication).userDependentComponent?.inject(this)

        val view = SyncViewMvcImpl(layoutInflater, null)
        controller = controllerFactory.create(
            this,
            view,
            supportFragmentManager,
            intent.getStringExtra(UUID_EXTRA_KEY)
        )

        controller?.onCreate()

        setContentView(view.rootView)
        setupAppBar(this, topAppBar)
    }

    override fun onStop() {
        super.onStop()
        controller?.onStop()
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
