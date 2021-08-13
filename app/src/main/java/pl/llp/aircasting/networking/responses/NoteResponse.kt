package pl.llp.aircasting.networking.responses

class NoteResponse (
    val sessionId: Long,
    val date: String,
    val text: String,
    val latitude: Double?,
    val longitude: Double?,
    val number: Int,
    val photoLocation: String? //todo: according to old app, maybe I do not need anything more then number and photoLocation????
)
