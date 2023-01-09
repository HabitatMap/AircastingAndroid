package pl.llp.aircasting.data.api.params


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DeleteAccountResponse(
    @SerializedName("error")
    val error: String?,
    @SerializedName("success")
    val success: Boolean?
)