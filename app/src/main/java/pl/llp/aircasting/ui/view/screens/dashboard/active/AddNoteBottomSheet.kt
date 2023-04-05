package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.app.Activity
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.android.synthetic.main.add_note_bottom_sheet.view.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.viewmodel.AddNoteBottomSheetViewModel
import pl.llp.aircasting.util.events.NoteCreatedEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.NotesNoLocationError
import pl.llp.aircasting.util.extensions.visible
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import java.util.*
import javax.inject.Inject

class AddNoteBottomSheet(
    private var mSession: Session?,
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

                    contentView?.captured_image?.apply {
                        setImageURI(fileUri)

                        mPhotoPath = fileUri.toString()
                        visible()
                    }

                }
                ImagePicker.RESULT_ERROR -> Toast.makeText(
                    requireContext(),
                    ImagePicker.getError(data),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun layoutId(): Int {
        return R.layout.add_note_bottom_sheet
    }

    override fun setup() {
        (requireActivity().application as AircastingApplication).userDependentComponent?.inject(this)

        checkIfCameraPermissionGranted()

        noteInput = contentView?.note_input
        contentView?.add_note_button?.setOnClickListener { addNote(mSession) }
        contentView?.add_picture_button?.setOnClickListener { takePictureUsingCamera() }
        contentView?.cancel_button?.setOnClickListener { dismiss() }
        contentView?.close_button?.setOnClickListener { dismiss() }
    }

    private fun addNote(mSession: Session?) = lifecycleScope.launch {
        val lastMeasurement = mSession?.lastMeasurement()
        if (lastMeasurement?.latitude == null || lastMeasurement.longitude == null) {
            mErrorHandler.handleAndDisplay(NotesNoLocationError())
            return@launch
        }
        val noteText = noteInput?.text.toString().trim()
        if (noteText.isEmpty()) {
            showEmptyError()
            return@launch
        }

        val noteDate = viewModel.lastAveragedMeasurementTime(mSession.uuid).first() ?: Date()
        val sessionNotes = mSession.notes
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
        val event = NoteCreatedEvent(mSession, note)
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

    private fun showCameraHelperDialog() {
        CameraPermissionHelperDialog(parentFragmentManager) {
            mPermissionsManager.requestCameraPermission(
                requireActivity()
            )
        }.show()
    }
}
