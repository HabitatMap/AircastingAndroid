package pl.llp.aircasting.ui.view.screens.dashboard

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.edit_session_bottom_sheet.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.data.model.TAGS_SEPARATOR
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.util.AnimatedLoader
import pl.llp.aircasting.util.showToast

class EditSessionBottomSheet(
    private val mListener: Listener,
    private var mLocalSession: LocalSession,
    private val mContext: Context?
) : BottomSheet() {
    interface Listener {
        fun onEditDataPressed(localSession: LocalSession, name: String, tags: ArrayList<String>)
    }

    private var sessionNameInputLayout: TextInputLayout? = null
    private var sessionNameInput: EditText? = null
    private var tagsInput: EditText? = null
    private var mLoader: ImageView? = null


    override fun layoutId(): Int {
        return R.layout.edit_session_bottom_sheet
    }

    override fun setup() {
        mLoader = contentView?.edit_loader

        sessionNameInputLayout = contentView?.session_name

        sessionNameInput = contentView?.session_name_input
        sessionNameInput?.setText(mLocalSession.name)

        tagsInput = contentView?.tags_input
        tagsInput?.setText(mLocalSession.tags.joinToString(TAGS_SEPARATOR))

        val editDataButton = contentView?.edit_data_button
        editDataButton?.setOnClickListener {
            onEditSessionPressed()
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

    fun reload(localSession: LocalSession) {
        mLocalSession = localSession
        sessionNameInput?.setText(mLocalSession.name)
        tagsInput?.setText(mLocalSession.tags.joinToString(TAGS_SEPARATOR))
    }

    fun showLoader() {
        AnimatedLoader(mLoader).start()
        mLoader?.visibility = View.VISIBLE
        sessionNameInput?.isEnabled = false
        tagsInput?.isEnabled = false
    }

    fun hideLoader() {
        AnimatedLoader(mLoader).stop()
        mLoader?.visibility = View.GONE
        sessionNameInput?.isEnabled = true
        tagsInput?.isEnabled = true
    }

    private fun onEditSessionPressed() {
        val name = sessionNameInput?.text.toString().trim()

        if (name.isEmpty()) {
            showError()
            return
        }

        val tags = tagsInput?.text.toString().trim()
        val tagList = ArrayList(tags.split(TAGS_SEPARATOR))

        dismiss()
        mListener.onEditDataPressed(mLocalSession, name, tagList)
    }

    private fun showError() {
        sessionNameInputLayout?.error = " "
        mContext?.showToast(getString(R.string.session_name_required), Toast.LENGTH_LONG)
    }
}
