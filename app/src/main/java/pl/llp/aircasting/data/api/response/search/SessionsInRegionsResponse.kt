package pl.llp.aircasting.data.api.response.search

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SessionsInRegionsResponse(
    @SerializedName("fetchableSessionsCount")
    val fetchableSessionsCount: Int,
    @SerializedName("sessions")
    val sessions: List<SessionInRegionResponse>
)