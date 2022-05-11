package pl.llp.aircasting.util

import android.content.Context
import pl.llp.aircasting.R
import retrofit2.HttpException
import javax.inject.Inject

open class ResponseHandler @Inject constructor(val mContext: Context) {
    fun <T : Any> handleSuccess(data: T): Resource<T> {
        return Resource.success(data)
    }

    fun <T : Any> handleException(e: Exception): Resource<T> {
        return when (e) {
            is HttpException -> Resource.error(null, getErrorMessage(e.code()))
            else -> Resource.error(null, getErrorMessage(Int.MAX_VALUE))
        }
    }

    private fun getErrorMessage(code: Int): String {
        return when (code) {
            401 -> "Unauthorised"
            404 -> "Not found"
            500 -> "Internal server error"
            else -> mContext.getString(R.string.sync_error_header)
        }
    }
}