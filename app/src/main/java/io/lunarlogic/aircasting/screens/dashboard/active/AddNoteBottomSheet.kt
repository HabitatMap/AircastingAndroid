package io.lunarlogic.aircasting.screens.dashboard.active

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.EditText
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Note
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.BottomSheet
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.*
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
    private var photoUri: Uri? = null
    val PICK_PHOTO_CODE = 1046

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

        val attachPhotoButton = contentView?.add_picture_button
        attachPhotoButton?.setOnClickListener {
            attachPhoto()
        }
    }

    private fun addNote(mSession: Session) {
        val noteText = noteInput?.text.toString().trim()
        val date = Date()
        val note = Note(
            date,
            noteText,
            mSession.streams[0].measurements.last().latitude,
            mSession.streams[0].measurements.last().longitude,
            mSession.notes.size,
            ""
        ) // todo: add "photoPath" later on, how to get this photoPath i want to add here

        mListener.addNotePressed(mSession, note)
    }

    private fun attachPhoto() {

        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(intent, PICK_PHOTO_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            photoUri = data.data  // this is the moment we get local path to our photo
        }

        val textView = contentView?.temp
        textView?.text = photoUri.toString()
    }
}
