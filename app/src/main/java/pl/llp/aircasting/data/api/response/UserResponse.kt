package pl.llp.aircasting.data.api.response

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val email: String,
    val username: String,
    @SerializedName("authentication_token")
    val authenticationToken: String,
    @SerializedName("session_stopped_alert")
    val sessionStoppedAlert: Boolean
)
