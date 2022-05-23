package pl.llp.aircasting.data.api.response.search

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SessionsInRegionsRes(
    @SerializedName("fetchableSessionsCount")
    val fetchableSessionsCount: Int,
    @SerializedName("sessions")
    val sessions: List<Session>
)