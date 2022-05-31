package pl.llp.aircasting.util.converters

import androidx.room.TypeConverter
import pl.llp.aircasting.data.model.LocalSession

class SessionTypeConverter {
    @TypeConverter
    fun fromInt(value: Int): LocalSession.Type {
        val type = LocalSession.Type.fromInt(value)
        return LocalSession.Type.valueOf(type.name)
    }

    @TypeConverter
    fun typeToInt(type: LocalSession.Type): Int {
        return type.value
    }
}
