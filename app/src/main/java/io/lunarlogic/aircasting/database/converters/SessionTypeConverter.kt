package io.lunarlogic.aircasting.database.converters

import androidx.room.TypeConverter
import io.lunarlogic.aircasting.models.Session

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
