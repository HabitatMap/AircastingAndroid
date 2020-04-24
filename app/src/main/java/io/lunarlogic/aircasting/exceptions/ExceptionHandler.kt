package io.lunarlogic.aircasting.exceptions

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast

class ExceptionHandler(private val mContext: Context): Handler(Looper.getMainLooper()) {
    override fun handleMessage(message: Message) {
        val exception = message.obj as BaseException
        handleAndDisplay(exception)
    }

    fun handle(exception: BaseException) {
        // TODO: crashlytics and logging
        exception.cause?.printStackTrace()
    }

    fun handleAndDisplay(exception: BaseException) {
        handle(exception)
        val toast = Toast.makeText(mContext, exception.messageToDisplay, Toast.LENGTH_LONG)
        toast.show()
    }
}