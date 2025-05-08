package pl.llp.aircasting.data.api.interceptor

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.InternetUnavailableException
import pl.llp.aircasting.util.extensions.isConnected
import javax.inject.Inject

class NetworkConnectionInterceptor @Inject constructor(
    private val errorHandler: ErrorHandler,
    private val context: AircastingApplication
) : Interceptor {
    private companion object {
        var hasShownNoConnectivityMessage = false
        var lastErrorTime = 0L
        const val ERROR_THROTTLE_MS = 5000L
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!context.isConnected) {
            showErrorMessageIfNotShownYet()
            return Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(503) // HTTP 503 Service Unavailable
                .message("No Internet Connection")
                .body("No Internet Connection".toResponseBody("text/plain".toMediaTypeOrNull()))
                .build()
        }
        return chain.proceed(chain.request())
    }

    private fun showErrorMessageIfNotShownYet() {
        val currentTime = System.currentTimeMillis()
        if (!hasShownNoConnectivityMessage || currentTime - lastErrorTime > ERROR_THROTTLE_MS) {
            errorHandler.handleAndDisplay(InternetUnavailableException())
            hasShownNoConnectivityMessage = true
            lastErrorTime = currentTime
        }
    }
}
