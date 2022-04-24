package pl.llp.aircasting.data.local.converters

import androidx.room.TypeConverter
import pl.llp.aircasting.data.model.Session

class SessionTypeConverter {
    @TypeConverter
    fun fromInt(value: Int): Session.Type {
        val type = Session.Type.fromInt(value)
        return Session.Type.valueOf(type.name)
    }

    @TypeConverter
    fun typeToInt(type: Session.Type): Int {
        return type.value
    }
}
