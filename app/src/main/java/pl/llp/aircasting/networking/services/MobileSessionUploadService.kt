package pl.llp.aircasting.networking.services

import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.UnexpectedAPIError
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.networking.GzippedParams
import pl.llp.aircasting.networking.params.CreateSessionBody
import pl.llp.aircasting.networking.params.SessionParams
import pl.llp.aircasting.networking.responses.UploadSessionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MobileSessionUploadService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    fun upload(session: Session, successCallback: (response: Response<UploadSessionResponse>) -> Unit) {
        val sessionParams = SessionParams(session)
        val sessionBody = CreateSessionBody(
            GzippedParams.get(sessionParams, SessionParams::class.java)
        )
        //TODO: somehow i have to use Gzipped params to create proper json of Notes <?>
        val call = apiService.createMobileSession(sessionBody)
        call.enqueue(object : Callback<UploadSessionResponse> {
            override fun onResponse(call: Call<UploadSessionResponse>, response: Response<UploadSessionResponse>) {
                if (response.isSuccessful) {
                    successCallback(response)
                } else {
                    errorHandler.handle(UnexpectedAPIError())
                }
            }

            override fun onFailure(call: Call<UploadSessionResponse>, t: Throwable) {
                errorHandler.handle(UnexpectedAPIError(t))
            }
        })
    }
}
