package pl.llp.aircasting.data.api.response

class NoteResponse(
    val sessionId: Long,
    val date: String,
    val text: String,
    val latitude: Double?,
    val longitude: Double?,
    val number: Int,
    val photoPath: String?
)
