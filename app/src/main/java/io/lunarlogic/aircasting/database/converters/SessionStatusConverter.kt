package io.lunarlogic.aircasting.database.converters

import androidx.room.TypeConverter
import io.lunarlogic.aircasting.sensor.Session

class SessionStatusConverter {
    @TypeConverter
    fun fromInt(value: Int): Session.Status {
        val status = Session.Status.values().filter { status -> status.value == value }.first()
        return Session.Status.valueOf(status.name)
    }

    @TypeConverter
    fun statusToInt(status: Session.Status): Int {
        return status.value
    }
}