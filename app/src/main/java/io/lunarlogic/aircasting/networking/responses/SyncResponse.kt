package io.lunarlogic.aircasting.networking.responses

class SyncResponse(val upload: List<String> = emptyList(), val download: List<String> = emptyList(), val deleted: List<String> = emptyList())