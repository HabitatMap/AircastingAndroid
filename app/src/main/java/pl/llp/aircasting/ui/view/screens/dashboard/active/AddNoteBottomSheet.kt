package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.content.Context
import android.widget.EditText
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.NotesNoLocationError
import java.util.*

class AddNoteBottomSheet(
    private val mListener: Listener,
    private var mSession: Session,
    private val mContext: Context?,
    private val mErrorHandler: ErrorHandler
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

        contentView?.add_note_button?.setOnClickListener {
            addNote(mSession)
            dismiss()
        }
        contentView?.add_picture_button?.setOnClickListener { addPictureButton() }
        contentView?.cancel_button?.setOnClickListener { dismiss() }
        contentView?.close_button?.setOnClickListener { dismiss() }
    }

    private fun addNote(mSession: Session) {
        val lastMeasurement = mSession.lastMeasurement()
        if (lastMeasurement?.latitude == null || lastMeasurement.longitude == null) {
            mErrorHandler.handleAndDisplay(NotesNoLocationError())
            return
        }

        val noteText = noteInput?.text.toString().trim()
        val date = Date()
        val note: Note
        if (mSession.notes.isEmpty())
            note = Note(
                date,
                noteText,
                lastMeasurement.latitude,
                lastMeasurement.longitude,
                0
            ) // todo: add "photoPath" later on
        else
            note = Note(
                date,
                noteText,
                lastMeasurement.latitude,
                lastMeasurement.longitude,
                mSession.notes.last().number + 1
            )

        mListener.addNotePressed(mSession, note)
    }

    private fun addPictureButton() {
        CameraPermissionHelperDialog(childFragmentManager).show()
    }
}
