package io.lunarlogic.aircasting.helpers

import com.google.gson.Gson

class JsonBody {
    companion object {
        val gson = Gson()

        fun build(body: Any): String {
            return gson.toJson(body)
        }
    }
}