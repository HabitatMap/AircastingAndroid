package pl.llp.aircasting.data.api.response

class SyncResponse(
    val upload: List<String> = emptyList(),
    val download: List<String> = emptyList(),
    val deleted: List<String> = emptyList()
)
