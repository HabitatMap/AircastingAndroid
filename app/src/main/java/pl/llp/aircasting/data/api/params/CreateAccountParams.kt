package pl.llp.aircasting.data.api.params

import com.google.gson.annotations.SerializedName

data class CreateAccountParams(
    val username: String,
    val password: String,
    val email: String,
    @SerializedName("send_emails")
    val sendEmails: Boolean = true,
    @SerializedName("session_stopped_alert")
    val sessionStoppedAlert: Boolean = true
)
