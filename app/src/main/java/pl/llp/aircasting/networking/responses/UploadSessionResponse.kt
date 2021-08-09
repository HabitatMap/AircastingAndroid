package pl.llp.aircasting.networking.responses

import pl.llp.aircasting.models.Note

class UploadSessionResponse(val location: String, val notes: List<Note>) //todo: Note probably should be changed for UploadNoteResponse
