package io.lunarlogic.aircasting.networking.responses

class SyncResponse(val upload: List<String>, val download: List<String>, val deleted: List<String>)