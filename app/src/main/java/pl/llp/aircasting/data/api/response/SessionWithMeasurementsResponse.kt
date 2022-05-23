package pl.llp.aircasting.data.api.response

class SessionWithMeasurementsResponse(
    val type: String,
    val uuid: String,
    val title: String,
    val tag_list: String,
    val start_time: String,
    val end_time: String,
    val deleted: Boolean,
    val version: Int,
    val streams: HashMap<String, SessionStreamWithMeasurementsResponse>,
    val notes: List<NoteResponse>

    // TODO: add contribute field after adding this functionallity

)
