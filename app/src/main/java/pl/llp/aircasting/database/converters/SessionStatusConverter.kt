package pl.llp.aircasting.database.converters

import androidx.room.TypeConverter
import pl.llp.aircasting.models.Session

class SessionStatusConverter {
    @TypeConverter
    fun fromInt(value: Int): Session.Status {
        val status = Session.Status.fromInt(value)
        return Session.Status.valueOf(status.name)
    }

    @TypeConverter
    fun statusToInt(status: Session.Status): Int {
        return status.value
    }
}
