package pl.llp.aircasting.util.exceptions

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import pl.llp.aircasting.BuildConfig
import pl.llp.aircasting.ui.view.common.AircastingAlertDialog
import pl.llp.aircasting.util.extensions.showToast
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorHandler @Inject constructor(private val mContext: Context) : Handler(Looper.getMainLooper()) {
    private val TAG = "ErrorHandler"
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun handleMessage(message: Message) {
        val exception = message.obj as BaseException
        handleAndDisplay(exception)
    }

    fun handle(exception: BaseException) {
        exception.cause?.printStackTrace()
        exception.messageToDisplay?.let { message ->
            if (exception.messageToDisplay is String) Log.e(
                TAG,
                message
            )
        }

        if (!BuildConfig.DEBUG) {
            exception.messageToDisplay?.let {
                crashlytics.log(it)
                crashlytics.recordException(exception)
            }
            exception.cause?.let { crashlytics.recordException(it) }
        }
    }

    fun handleAndDisplay(exception: BaseException) {
        handle(exception)
        showError(exception.messageToDisplay)
    }

    fun showError(message: String?) {
        message?.let {
            mContext.showToast(it, Toast.LENGTH_LONG)
        }
    }

    fun showError(messageResId: Int) {
        val message = mContext.getString(messageResId)
        showError(message)
    }

    fun showErrorDialog(fragmentManager: FragmentManager?, header: String?, description: String?) {
        fragmentManager ?: return

        val dialog = AircastingAlertDialog(fragmentManager, header, description)
        dialog.show()
    }

    fun registerUser(email: String?) {
        if (!BuildConfig.DEBUG) {
            email?.let { crashlytics.setUserId(it) }
        }
    }
}
