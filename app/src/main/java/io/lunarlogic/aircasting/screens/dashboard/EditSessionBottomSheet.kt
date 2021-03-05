package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.AnimatedLoader
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.TAGS_SEPARATOR
import io.lunarlogic.aircasting.screens.common.BottomSheet
import kotlinx.android.synthetic.main.edit_session_bottom_sheet.view.*

class EditSessionBottomSheet(
    private val mListener: Listener,
    private var mSession: Session,
    private val mContext: Context?
) : BottomSheet() {
    interface Listener{
        fun onEditDataPressed(session: Session, name: String, tags: ArrayList<String>)
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
        sessionNameInput?.setText(mSession.name)

        tagsInput = contentView?.tags_input
        tagsInput?.setText(mSession.tags.joinToString(TAGS_SEPARATOR))

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

    fun reload(session: Session) {
        mSession = session
        sessionNameInput?.setText(mSession.name)
        tagsInput?.setText(mSession.tags.joinToString(TAGS_SEPARATOR))
    }

    fun showLoader() {
        AnimatedLoader(mLoader).start()
        mLoader?.visibility = View.VISIBLE
        sessionNameInput?.isEnabled = false
        tagsInput?.isEnabled = false
    }

    fun hideLoader() {
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
        mListener.onEditDataPressed(mSession, name, tagList)
    }

    private fun showError() {
        sessionNameInputLayout?.error = " "
        Toast.makeText(mContext, getString(R.string.session_name_required), Toast.LENGTH_LONG).show()
    }
}
