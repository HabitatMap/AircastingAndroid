package pl.llp.aircasting.networking.responses

class NoteResponse (
    val sessionId: Long,
    val date: String,
    val text: String,
    val latitude: Double?,
    val longitude: Double?,
    val number: Int
    // todo: photoPath to be added when handling photos
)
