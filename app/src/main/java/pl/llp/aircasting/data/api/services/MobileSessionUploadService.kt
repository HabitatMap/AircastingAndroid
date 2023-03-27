package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.GzippedParams
import pl.llp.aircasting.data.api.params.CreateSessionBody
import pl.llp.aircasting.data.api.params.SessionParams
import pl.llp.aircasting.data.api.response.UploadSessionResponse
import pl.llp.aircasting.data.model.Session
import retrofit2.Response
import javax.inject.Inject

class MobileSessionUploadService @Inject constructor(
    @Authenticated private val apiService: ApiService
) {
    suspend fun upload(
        session: Session,
        photosAsBase64String: List<String?>
    ): Response<UploadSessionResponse> {
        val sessionParams = SessionParams(session)
        val sessionBody =
            CreateSessionBody(
                GzippedParams.get(sessionParams, SessionParams::class.java),
                photos = photosAsBase64String
            )

        return apiService.createMobileSession(sessionBody)
    }
}
