package io.lunarlogic.aircasting.screens.dashboard.active

import android.content.Context
import android.widget.EditText
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Note
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.BottomSheet
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.*
import java.time.Instant.now
import java.util.*

class AddNoteBottomSheet(
    private val mListener: Listener,
    private var mSession: Session,
    private val mContext: Context?
) : BottomSheet() {
    interface Listener {
        fun addNotePressed(session: Session, note: Note)
    }

    private var noteInput: EditText? = null

    override fun layoutId(): Int {
        return R.layout.add_note_bottom_sheet
    }

    override fun setup() {
        noteInput = contentView?.note_input

        // button listeners
        val addNoteButton = contentView?.add_note_button
        addNoteButton?.setOnClickListener {
            addNote(mSession)
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

    private fun addNote(mSession: Session) {
        val noteText = noteInput?.text.toString().trim()
        val date = Date()
        //todo: here we need to get sessionID from sessionUUID somehow <?>
        val note = Note(date, noteText, mSession.location?.latitude, mSession.location?.longitude, mSession.notes.size) // ,"photoPath" todo: random data for now

        mListener.addNotePressed(mSession, note)
    }
}
