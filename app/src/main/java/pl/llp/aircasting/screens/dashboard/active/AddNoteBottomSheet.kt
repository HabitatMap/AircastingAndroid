package pl.llp.aircasting.screens.dashboard.active

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import pl.llp.aircasting.R
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.NotesNoLocationError
import pl.llp.aircasting.models.Note
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.common.BottomSheet
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
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
    private var notePhotoImageView: ImageView? = null
    private var photoUri: Uri? = null
    private var photoPath: String = ""
    private var imageEncoded: String = ""
    val PICK_PHOTO_CODE = 1046
    val CAMERA_REQUEST_CODE = 1888
    val REQUEST_IMAGE_CAPTURE = 1

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
            dispatchTakePictureIntent()
        }

        notePhotoImageView = contentView?.note_photo_image_view
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
            note = Note(date, noteText, lastMeasurement.latitude, lastMeasurement.longitude, 0, photoPath)
        else
            note = Note(date, noteText, lastMeasurement.latitude, lastMeasurement.longitude, mSession.notes.last().number + 1, photoPath)

        Log.i("PHOTO", "after creating note, photoPath: " + note.photoPath.toString())
        mListener.addNotePressed(mSession, note)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val inputStream = context?.contentResolver?.openInputStream(Uri.parse(photoPath))
            val imageBitmap = BitmapFactory.decodeStream(inputStream)
            Log.i("PHOTO", "onActivityResult, imageBitmap: " + imageBitmap.toString())
            notePhotoImageView?.setImageBitmap(imageBitmap)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            photoPath = absolutePath
            Log.i("PHOTO", "createImageFile, photoPath: " + photoPath)
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(mContext!!.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        mContext,
                        "pl.llp.aircasting.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI) // this photoURI should store taken picture
                    Log.i("PHOTO", "photoUri: " + photoURI.toString())
                    photoPath = photoURI.toString()
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
                }
            }
        }
    }

}
