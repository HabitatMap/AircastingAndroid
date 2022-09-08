package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.NotesNoLocationError
import pl.llp.aircasting.util.extensions.visible
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import java.util.*

class AddNoteBottomSheet(
    private val mListener: Listener,
    private var mSession: Session,
    private val mContext: Context?,
    private val mErrorHandler: ErrorHandler,
    private val mPermissionsManager: PermissionsManager = PermissionsManager()
) : BottomSheet() {
    interface Listener {
        fun addNotePressed(session: Session, note: Note)
        fun showCameraHelperDialog()
    }

    private var noteInput: EditText? = null
    private var mPhotoPath: Uri? = null
    private val mDate: Date = Date()

    override fun layoutId(): Int {
        return R.layout.add_note_bottom_sheet
    }

    override fun setup() {
        noteInput = contentView?.note_input

        contentView?.add_note_button?.setOnClickListener {
            addNote(mSession)
        }
        contentView?.add_picture_button?.setOnClickListener { checkIfPermissionsGranted() }
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
        val note: Note?

        if (noteText.isNotEmpty()) {

            val mSessionNotes = mSession.notes

            if (mSessionNotes.isNotEmpty()) {
                note = Note(
                    mDate,
                    noteText,
                    lastMeasurement.latitude,
                    lastMeasurement.longitude,
                    mSessionNotes.last().number + 1,
                    mPhotoPath
                )
                mListener.addNotePressed(mSession, note)
                dismiss()
            } else {
                note = Note(
                    mDate,
                    noteText,
                    lastMeasurement.latitude,
                    lastMeasurement.longitude,
                    0,
                    mPhotoPath
                )
                mListener.addNotePressed(mSession, note)
                dismiss()
            }
        } else showEmptyError()
    }

    private fun showEmptyError() {
        contentView?.note_input_layout?.apply {
            isErrorEnabled = true
            error = mContext?.getString(R.string.notes_des_cant_be_empty)
        }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data
                    fileUri ?: return@registerForActivityResult

                    contentView?.captured_image?.apply {
                        setImageURI(fileUri)
                        mPhotoPath = fileUri
                        visible()
                    }

                }
                ImagePicker.RESULT_ERROR -> Toast.makeText(
                    mContext,
                    ImagePicker.getError(data),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun checkIfPermissionsGranted() {
        mContext ?: return

        if (mPermissionsManager.cameraPermissionGranted(mContext)
        ) takePictureUsingCamera() else showCameraHelperDialog()
    }

    /**
     * This is customizable - currently we have crop option + camera only.
     * We are also able to add the gallery option to this library.
     * More info: https://github.com/Dhaval2404/ImagePicker#customization
     **/
    private fun takePictureUsingCamera() {
        ImagePicker.with(this)
            .maxResultSize(620, 620)
            .crop()
            .cameraOnly()
            .createIntent { intent -> startForProfileImageResult.launch(intent) }
    }

    private fun showCameraHelperDialog() {
        mListener.showCameraHelperDialog()
    }
}
