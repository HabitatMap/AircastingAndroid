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
            401 -> mContext.getString(R.string.unauth)
            404 -> mContext.getString(R.string.not_found)
            500 -> mContext.getString(R.string.internal_error)
            else -> mContext.getString(R.string.sync_error_header)
        }
    }
}