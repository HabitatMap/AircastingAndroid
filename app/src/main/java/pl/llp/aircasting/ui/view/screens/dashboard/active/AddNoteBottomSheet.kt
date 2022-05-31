package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.content.Context
import android.widget.EditText
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.NotesNoLocationError
import java.util.*

class AddNoteBottomSheet(
    private val mListener: Listener,
    private var mLocalSession: LocalSession,
    private val mContext: Context?,
    private val mErrorHandler: ErrorHandler
) : BottomSheet() {
    interface Listener {
        fun addNotePressed(localSession: LocalSession, note: Note)
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
            addNote(mLocalSession)
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

    private fun addNote(mLocalSession: LocalSession) {
        val lastMeasurement = mLocalSession.lastMeasurement()
        if (lastMeasurement?.latitude == null || lastMeasurement.longitude == null) {
            mErrorHandler.handleAndDisplay(NotesNoLocationError())
            return
        }

        val noteText = noteInput?.text.toString().trim()
        val date = Date()
        var note: Note
        if (mLocalSession.notes.isNullOrEmpty())
            note = Note(date, noteText, lastMeasurement.latitude, lastMeasurement.longitude, 0) // todo: add "photoPath" later on
        else
            note = Note(date, noteText, lastMeasurement.latitude, lastMeasurement.longitude, mLocalSession.notes.last().number + 1)

        mListener.addNotePressed(mLocalSession, note)
    }
}
