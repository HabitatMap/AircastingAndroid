package io.lunarlogic.aircasting.exceptions

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import com.google.firebase.crashlytics.FirebaseCrashlytics

class ErrorHandler(private val mContext: Context): Handler(Looper.getMainLooper()) {
    val mFirebase = FirebaseCrashlytics.getInstance()

    override fun handleMessage(message: Message) {
        val exception = message.obj as BaseException
        handleAndDisplay(exception)
    }

    fun handle(exception: BaseException) {
        exception.cause?.printStackTrace()
        exception.messageToDisplay?.let { mFirebase.log(it) }
        exception.cause?.let { mFirebase.recordException(it) }
    }

    fun handleAndDisplay(exception: BaseException) {
        handle(exception)
        showError(exception.messageToDisplay)
    }

    fun showError(message: String?) {
        message?.let {
            val toast = Toast.makeText(mContext, it, Toast.LENGTH_LONG)
            toast.show()
        }
    }

    fun showError(messageResId: Int) {
        val message = mContext.getString(messageResId)
        showError(message)
    }
}