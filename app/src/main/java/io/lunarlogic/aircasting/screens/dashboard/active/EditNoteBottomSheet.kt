package io.lunarlogic.aircasting.screens.dashboard.active

import android.widget.EditText
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Note
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.BottomSheet
import kotlinx.android.synthetic.main.edit_note_bottom_sheet.view.*

class EditNoteBottomSheet(
    private val mListener: Listener,
    private val mSession: Session?, //todo: maybe i should do it in a smarter way ?
    private val noteNumber: Int
): BottomSheet() {
    interface Listener {
        fun saveChangesNotePressed(note: Note?, session: Session?)
        fun deleteNotePressed(note: Note?)
    }
    private var mNote: Note? = null
    private var noteInput: EditText? = null

    override fun setup() {
        noteInput = contentView?.note_input
        mNote = mSession?.notes?.get(noteNumber) // getting note by index will not always work (we need to check mSession.notes for a note with certain number
        noteInput?.setText(mNote?.text)

        val saveChangesButton = contentView?.save_changes_button
        saveChangesButton?.setOnClickListener {
            saveChanges()
            dismiss()
        }

        val deleteNoteButton = contentView?.delete_note_button
        deleteNoteButton?.setOnClickListener {
            deleteNote()
            dismiss()
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

    private fun saveChanges() {
        val noteText = noteInput?.text.toString().trim()
        mNote?.text = noteText
        mListener.saveChangesNotePressed(mNote, mSession)
        //TODO("Not yet implemented") // to be filled when working on edit note ticket
    }

    private fun deleteNote() {
        TODO("Not yet implemented") // to be filled when working on delete note ticket
    }

    override fun layoutId(): Int {
        return R.layout.edit_note_bottom_sheet
    }

}
