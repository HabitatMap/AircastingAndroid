package pl.llp.aircasting.util

import pl.llp.aircasting.data.api.response.NoteResponse
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.ParseDateError
import java.text.ParseException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteResponseParser @Inject constructor(
    private val errorHandler: ErrorHandler
) {
    fun noteFromResponse(noteResponse: NoteResponse): Note {
        return Note(
            parseDate(noteResponse.date),
            noteResponse.text,
            noteResponse.latitude,
            noteResponse.longitude,
            noteResponse.number,
            noteResponse.photo_location
        )
    }

    private fun parseDate(date: String): Date {
        return try {
            DateConverter.fromString(date) ?: Date()
        } catch (parseException: ParseException) {
            errorHandler.handle(ParseDateError(parseException))
            Date()
        }
    }

}
