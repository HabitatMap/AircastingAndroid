package pl.llp.aircasting.util

import pl.llp.aircasting.data.api.response.NoteResponse
import pl.llp.aircasting.data.api.util.ApiConstants
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.ParseDateError
import java.text.ParseException
import java.util.*

class NoteResponseParser(private val errorHandler: ErrorHandler) {
    fun noteFromResponse(noteResponse: NoteResponse): Note {
            return Note(
                parseDate(noteResponse.date),
                noteResponse.text,
                noteResponse.latitude,
                noteResponse.longitude,
                noteResponse.number,
                ApiConstants.baseUrl + noteResponse.photo
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
