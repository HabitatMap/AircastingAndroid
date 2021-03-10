package io.lunarlogic.aircasting.screens.dashboard.active

import android.content.Context
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.BottomSheet
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.*

class AddNoteBottomSheet(
//    private val mListener: Listener,
    private var mSession: Session,
    private val mContext: Context?
) : BottomSheet() {
    interface Listener {
        fun addNotePressed()
    }

    override fun layoutId(): Int {
        return R.layout.add_note_bottom_sheet
    }

    override fun setup() {
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
    }

    private fun addNote(mSession: Session) {
        // TODO: here we need to take date, text of note and the rest things to model
        //todo: where should i send the NoteCreatedEvent <??>
    }
}
