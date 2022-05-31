package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.edit_note_bottom_sheet.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.ConnectivityManager
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.util.AnimatedLoader
import pl.llp.aircasting.util.showToast

class EditNoteBottomSheet(
    private val mListener: Listener,
    private var mLocalSession: LocalSession?,
    private val noteNumber: Int
) : BottomSheet() {
    interface Listener {
        fun saveChangesNotePressed(note: Note?, localSession: LocalSession?)
        fun deleteNotePressed(note: Note?, localSession: LocalSession?)
    }

    private var mNote: Note? = null
    private var noteInput: EditText? = null
    private var mLoader: ImageView? = null

    override fun setup() {
        noteInput = contentView?.note_input
        mNote = mLocalSession?.notes?.find { note -> note.number == noteNumber }
        noteInput?.setText(mNote?.text)
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
        cancelButton?.setOnClickListener {
            dismiss()
        }

        val closeButton = contentView?.close_button
        closeButton?.setOnClickListener {
            dismiss()
        }

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
        mListener.saveChangesNotePressed(mNote, mLocalSession)
    }

    private fun deleteNote() {
        mListener.deleteNotePressed(mNote, mLocalSession)
    }

    override fun layoutId(): Int {
        return R.layout.edit_note_bottom_sheet
    }

    fun reload(localSession: LocalSession) {
        mLocalSession = localSession
        noteInput?.setText(mNote?.text)
    }

    fun showLoader() {
        AnimatedLoader(mLoader).start()
        mLoader?.visibility = View.VISIBLE
        noteInput?.isEnabled = false
    }

    fun hideLoader() {
        AnimatedLoader(mLoader).stop()
        mLoader?.visibility = View.GONE
        noteInput?.isEnabled = true
    }

}
