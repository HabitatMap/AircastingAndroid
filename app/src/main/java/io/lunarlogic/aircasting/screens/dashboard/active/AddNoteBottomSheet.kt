package io.lunarlogic.aircasting.screens.dashboard.active

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.PhotoHelper
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
    private var imageEncoded: String = ""
    val PICK_PHOTO_CODE = 1046
    val REQUEST_IMAGE_PICTURE = 1

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
            imageEncoded   // TODO: FOR SOME REASON MOBILE DORMANT SESSION WITH NOTE WITH PHOTOPATH ARE NOT DOWNLOADED/DISPLAYED!!!
        )

        mListener.addNotePressed(mSession, note)
    }

    private fun attachPhoto() {

        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        // TODO: ask for camera permision <?>
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            startActivityForResult(intent, PICK_PHOTO_CODE)
        } catch (e: ActivityNotFoundException) {
            Log.e("ATTACH_PHOTO", "Cant take picture")
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            photoUri = data.data  // this is the moment we get local path to our photo
        }

        val bitmap = MediaStore.Images.Media.getBitmap(this.context?.contentResolver, photoUri)
        imageEncoded = PhotoHelper.getBase64String(bitmap)
    }
}
