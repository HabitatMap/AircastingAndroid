package pl.llp.aircasting.lib

import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.ParseDateError
import pl.llp.aircasting.models.Note
import pl.llp.aircasting.networking.responses.NoteResponse
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
