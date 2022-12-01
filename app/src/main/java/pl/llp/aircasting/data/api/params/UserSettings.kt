package pl.llp.aircasting.data.api.params

import com.google.gson.annotations.SerializedName

data class UserSettingsBody(
    val data: UserSettingsData
)

data class UserSettingsData(
    @SerializedName("session_stopped_alert")
    val sessionStoppedAlert: Boolean = true
)

class UserSettingsResponse