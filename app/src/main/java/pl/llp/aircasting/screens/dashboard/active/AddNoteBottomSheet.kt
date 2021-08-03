package pl.llp.aircasting.screens.dashboard.active

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
import pl.llp.aircasting.R
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.NotesNoLocationError
import pl.llp.aircasting.models.Note
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.common.BottomSheet
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.*
import java.io.ByteArrayOutputStream
import java.io.File
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
        val lastMeasurement = mSession.lastMeasurement()
        if (lastMeasurement.latitude == null || lastMeasurement.longitude == null) {
            mErrorHandler.handleAndDisplay(NotesNoLocationError())
            return
        }

        val noteText = noteInput?.text.toString().trim()
        val date = Date()
        var note: Note
        if (mSession.notes.isNullOrEmpty())
            note = Note(date, noteText, lastMeasurement.latitude, lastMeasurement.longitude, 0, imageEncoded)
        else
            note = Note(date, noteText, lastMeasurement.latitude, lastMeasurement.longitude, mSession.notes.last().number + 1, imageEncoded)

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
            //imageEncoded = PhotoHelper.getBase64String(imageBitmap)

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
