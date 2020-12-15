package io.lunarlogic.aircasting.database.converters

import androidx.room.TypeConverter
import io.lunarlogic.aircasting.models.Session

class SessionStatusListConverter {
    companion object {
        fun fromIntList(list: List<Int>): List<Session.Status> {
            val statuses = list.map { value ->
                val status = Session.Status.fromInt(value)
                Session.Status.valueOf(status.name)
            }

            return statuses
        }

        fun toIntList(statuses: List<Session.Status>): List<Int> {
            return statuses.map { it.value }
        }
    }
}
