package pl.llp.aircasting.util.converters

import androidx.room.TypeConverter
import pl.llp.aircasting.data.model.LocalSession

class SessionStatusConverter {
    @TypeConverter
    fun fromInt(value: Int): LocalSession.Status {
        val status = LocalSession.Status.fromInt(value)
        return LocalSession.Status.valueOf(status.name)
    }

    @TypeConverter
    fun statusToInt(status: LocalSession.Status): Int {
        return status.value
    }
}
