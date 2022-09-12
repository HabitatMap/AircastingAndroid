package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.edit_note_bottom_sheet.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.ConnectivityManager
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.util.extensions.showToast
import pl.llp.aircasting.util.extensions.startAnimation
import pl.llp.aircasting.util.extensions.stopAnimation

class EditNoteBottomSheet(
    private val mListener: Listener,
    private var mSession: Session?,
    private val noteNumber: Int
) : BottomSheet() {
    interface Listener {
        fun saveChangesNotePressed(note: Note?, session: Session?)
        fun deleteNotePressed(note: Note?, session: Session?)
    }

    private var mNote: Note? = null
    private var noteInput: EditText? = null
    private var mLoader: ImageView? = null
    private val mNoteImage: ImageView? = null

    override fun setup() {
        noteInput = contentView?.note_input
        mNote = mSession?.notes?.find { note -> note.number == noteNumber }
        noteInput?.setText(mNote?.text)

        val mPhoto = mSession?.notes?.find { note -> note.number == noteNumber }?.photo_location
        mNoteImage?.setImageURI(mPhoto?.toUri())
        // Load from Db

        mLoader = contentView?.edit_note_loader

        val saveChangesButton = contentView?.save_changes_button
        saveChangesButton?.setOnClickListener {
            saveChanges()
            dismiss()
        }

        val deleteNoteButton = contentView?.delete_note_button
        deleteNoteButton?.setOnClickListener {
            deleteNote()
            dismiss()
        }

        val cancelButton = contentView?.cancel_button
        cancelButton?.setOnClickListener { dismiss() }

        val closeButton = contentView?.close_button
        closeButton?.setOnClickListener { dismiss() }

        showLoader()
    }

    private fun saveChanges() {
        if (!ConnectivityManager.isConnected(context)) {
            context?.apply {
                showToast(
                    getString(R.string.errors_network_required_edit),
                    Toast.LENGTH_LONG
                )
            }
            return
        }

        val noteText = noteInput?.text.toString().trim()
        mNote?.text = noteText
        mListener.saveChangesNotePressed(mNote, mSession)
    }

    private fun deleteNote() {
        mListener.deleteNotePressed(mNote, mSession)
    }

    override fun layoutId(): Int {
        return R.layout.edit_note_bottom_sheet
    }

    fun reload(session: Session) {
        mSession = session
        noteInput?.setText(mNote?.text)
    }

    fun showLoader() {
        mLoader?.startAnimation()
        mLoader?.visibility = View.VISIBLE
        noteInput?.isEnabled = false
    }

    fun hideLoader() {
        mLoader?.stopAnimation()
        mLoader?.visibility = View.GONE
        noteInput?.isEnabled = true
    }

}
