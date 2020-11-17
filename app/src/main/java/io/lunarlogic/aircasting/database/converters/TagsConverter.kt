package io.lunarlogic.aircasting.database.converters

import androidx.room.TypeConverter
import io.lunarlogic.aircasting.models.TAGS_SEPARATOR

class TagsConverter {
    @TypeConverter
    fun fromString(value: String): ArrayList<String> {
        if (value.isEmpty()) return arrayListOf()

        return ArrayList(value.split(TAGS_SEPARATOR))
    }

    @TypeConverter
    fun tagsToString(tags: ArrayList<String>): String {
        return tags.joinToString(TAGS_SEPARATOR)
    }
}
