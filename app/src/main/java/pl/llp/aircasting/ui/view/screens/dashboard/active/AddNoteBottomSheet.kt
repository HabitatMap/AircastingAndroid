package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.app.Activity
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.add_note_button
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.add_picture_button
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.cancel_button
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.captured_image
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.close_button
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.note_input
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.note_input_layout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.viewmodel.AddNoteBottomSheetViewModel
import pl.llp.aircasting.util.events.NoteCreatedEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.NotesNoLocationError
import pl.llp.aircasting.util.extensions.visible
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import javax.inject.Inject

class AddNoteBottomSheet(
    private var sessionUuid: String?,
) : BottomSheet() {
    constructor() : this(null)

    private var noteInput: EditText? = null
    private var mPhotoPath: String? = null

    @Inject
    lateinit var mErrorHandler: ErrorHandler

    @Inject
    lateinit var mPermissionsManager: PermissionsManager

    @Inject
    lateinit var viewModel: AddNoteBottomSheetViewModel

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data
                    fileUri ?: return@registerForActivityResult

                    mPhotoPath = correctImageOrientationIfNeeded(fileUri).also { photoPath ->
                        contentView?.captured_image?.apply {
                            setImageURI(photoPath.toUri())
                            visible()
                        }
                    }
                }

                ImagePicker.RESULT_ERROR -> Toast.makeText(
                    requireContext(),
                    ImagePicker.getError(data),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("sessionUuid", sessionUuid)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        sessionUuid = savedInstanceState?.getString("sessionUuid") ?: sessionUuid
        return super.onCreateDialog(savedInstanceState)
    }

    override fun layoutId(): Int {
        return R.layout.add_note_bottom_sheet
    }

    override fun setup() {
        (requireActivity().application as AircastingApplication).userDependentComponent?.inject(this)

        checkIfCameraPermissionGranted()

        noteInput = contentView?.note_input
        contentView?.add_note_button?.setOnClickListener { addNote() }
        contentView?.add_picture_button?.setOnClickListener { takePictureUsingCamera() }
        contentView?.cancel_button?.setOnClickListener { dismiss() }
        contentView?.close_button?.setOnClickListener { dismiss() }
    }

    private fun addNote() = lifecycleScope.launch {
        val session = viewModel.getSessionByUUID(sessionUuid).first()
        val lastMeasurement = session.lastMeasurement()
        if (lastMeasurement?.latitude == null || lastMeasurement.longitude == null) {
            mErrorHandler.handleAndDisplay(NotesNoLocationError())
            return@launch
        }
        val noteText = noteInput?.text.toString().trim()
        if (noteText.isEmpty()) {
            showEmptyError()
            return@launch
        }

        val noteDate = viewModel.lastAveragedMeasurementTime(sessionUuid).first() ?: Date()
        val sessionNotes = session.notes
        val noteNumber = if (sessionNotes.isNotEmpty())
            sessionNotes.last().number + 1
        else
            0

        val note = Note(
            noteDate,
            noteText,
            lastMeasurement.latitude,
            lastMeasurement.longitude,
            noteNumber,
            mPhotoPath
        )
        val event = NoteCreatedEvent(session, note)
        EventBus.getDefault().post(event)
        dismiss()
    }

    private fun showEmptyError() {
        contentView?.note_input_layout?.apply {
            isErrorEnabled = true
            error = context.getString(R.string.notes_description_empty_error)
        }
    }

    private fun checkIfCameraPermissionGranted() {
        if (!mPermissionsManager.cameraPermissionGranted(requireContext())) showCameraHelperDialog()
    }

    /**
     * This is customizable - currently we have crop option + camera only.
     * We are also able to add the gallery option to this library.
     * More info: https://github.com/Dhaval2404/ImagePicker#customization
     **/
    private fun takePictureUsingCamera() {
        ImagePicker.with(this)
            .maxResultSize(620, 620)
            .compress(500)
            .cameraOnly()
            .createIntent { intent -> startForProfileImageResult.launch(intent) }
    }

    private fun correctImageOrientationIfNeeded(uri: Uri): String {
        try {
            val context = requireContext()
            val inputStream = context.contentResolver.openInputStream(uri) ?: return uri.toString()

            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            if (orientation == ExifInterface.ORIENTATION_NORMAL) {
                return uri.toString()
            }

            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

            val rotationAngle = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            val rotatedBitmap = if (rotationAngle != 0) {
                val matrix = Matrix()
                matrix.postRotate(rotationAngle.toFloat())
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

            val filename = "IMG_${System.currentTimeMillis()}.jpg"
            val file = File(context.cacheDir, filename)
            val outputStream = FileOutputStream(file)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            return Uri.fromFile(file).toString()

        } catch (e: Exception) {
            Log.e("ImageRotation", "Error correcting image orientation", e)
            return uri.toString()
        }
    }

    private fun showCameraHelperDialog() {
        CameraPermissionHelperDialog(parentFragmentManager) {
            mPermissionsManager.requestCameraPermission(
                requireActivity()
            )
        }.show()
    }
}
