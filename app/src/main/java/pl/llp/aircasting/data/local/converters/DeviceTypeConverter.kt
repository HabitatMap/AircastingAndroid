package pl.llp.aircasting.data.local.converters

import androidx.room.TypeConverter
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem

class DeviceTypeConverter {
    @TypeConverter
    fun fromInt(value: Int?): DeviceItem.Type? {
        value ?: return null
        
        val type = DeviceItem.Type.fromInt(value)
        return DeviceItem.Type.valueOf(type.name)
    }

    @TypeConverter
    fun typeToInt(type: DeviceItem.Type?): Int? {
        return type?.value
    }
}
