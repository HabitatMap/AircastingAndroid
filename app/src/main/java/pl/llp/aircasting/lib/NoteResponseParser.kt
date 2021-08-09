package pl.llp.aircasting.lib

import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.ParseDateError
import pl.llp.aircasting.models.Note
import pl.llp.aircasting.networking.responses.NoteResponse
import java.text.ParseException
import java.util.*

class NoteResponseParser(private val errorHandler: ErrorHandler) {
    fun noteFromResponse(noteResponse: NoteResponse): Note {
        try {
            return Note(
                DateConverter.fromString(noteResponse.date)!!,
                noteResponse.text,
                noteResponse.latitude,
                noteResponse.longitude,
                noteResponse.number
            )
        } catch (parseDateError: ParseDateError) {
            errorHandler.handle(parseDateError)
            return Note(
                Date(),
                noteResponse.text,
                noteResponse.latitude,
                noteResponse.longitude,
                noteResponse.number
            )
        }
    }
}
