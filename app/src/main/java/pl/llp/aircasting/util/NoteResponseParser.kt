package pl.llp.aircasting.util

import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.ParseDateError
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.api.responses.NoteResponse
import java.text.ParseException
import java.util.*

class NoteResponseParser(private val errorHandler: ErrorHandler) {
    fun noteFromResponse(noteResponse: NoteResponse): Note {
            return Note(
                parseDate(noteResponse.date),
                noteResponse.text,
                noteResponse.latitude,
                noteResponse.longitude,
                noteResponse.number
            )
    }

    private fun parseDate(date: String): Date {
        try {
            return DateConverter.fromString(date) ?: Date()
        } catch (parseException: ParseException) {
            errorHandler.handle(ParseDateError(parseException))
            return Date()
        }
    }
}