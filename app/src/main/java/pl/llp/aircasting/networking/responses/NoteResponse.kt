package pl.llp.aircasting.networking.responses

class NoteResponse (
    val sessionId: Long,
    val date: Long,
    val text: String,
    val latitude: Double?,
    val longitude: Double?,
    val number: Int,
    val photoPath: String?
)
