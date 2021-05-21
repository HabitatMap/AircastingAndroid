package io.lunarlogic.aircasting.screens.dashboard.active

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
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
import java.io.ByteArrayOutputStream
import java.io.File
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
    private var imageEncoded: String = "pp"
    val PICK_PHOTO_CODE = 1046
    val CAMERA_REQUEST_CODE = 1888

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
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            Log.e("ATTACH_PHOTO", "Cant take picture")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode === CAMERA_REQUEST_CODE && resultCode === Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageEncoded = PhotoHelper.getBase64String(imageBitmap)

            val tempUri = getImageUri(mContext!!, imageBitmap)
            val finalFile = File(getRealPathFromURI(tempUri))
            imageEncoded = finalFile.path
            Log.i("ADD_NOTE", imageEncoded)
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        return mContext?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    fun getRealPathFromURI(uri: Uri?): String? {
        var path = ""
        if (mContext?.contentResolver != null) {
            val cursor: Cursor? = mContext.contentResolver?.query(uri!!, null, null, null, null) //todo: null assertions
            if (cursor != null) {
                cursor.moveToFirst()
                val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }

}
