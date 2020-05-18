package io.lunarlogic.aircasting.database.converters

import androidx.room.TypeConverter
import io.lunarlogic.aircasting.sensor.TAGS_SEPARATOR

class TagsConverter {
    @TypeConverter
    fun fromString(value: String?): List<String>? {
        return value?.let { value.split(TAGS_SEPARATOR) }
    }

    @TypeConverter
    fun tagsToString(tags: List<String>?): String? {
        return tags?.joinToString(TAGS_SEPARATOR)
    }
}