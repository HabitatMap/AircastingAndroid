package io.lunarlogic.aircasting.screens.dashboard.active

import android.widget.EditText
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Note
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.BottomSheet
import kotlinx.android.synthetic.main.edit_note_bottom_sheet.view.*

class EditNoteBottomSheet(
    private val mListener: Listener,
    private val sessionId: String //todo: soon maybe to be changed on Long- noteId
): BottomSheet() {
    interface Listener {
        fun editNotePressed(markerId: String)
        fun deleteNotePressed(note: Note)
    }

    private var noteInput: EditText? = null

    override fun setup() {
        noteInput = contentView?.note_input
        noteInput?.setText(sessionId) //todo: for now just to check if works fine

        val editNoteButton = contentView?.edit_note_button
        editNoteButton?.setOnClickListener {
            editNote()
        }

        val cancelButton = contentView?.cancel_button
        cancelButton?.setOnClickListener {
            dismiss()
        }

        val closeButton = contentView?.close_button
        closeButton?.setOnClickListener {
            dismiss()
        }
    }

    private fun editNote() {
        TODO("Not yet implemented")
    }

    override fun layoutId(): Int {
        return R.layout.edit_note_bottom_sheet
    }


}
