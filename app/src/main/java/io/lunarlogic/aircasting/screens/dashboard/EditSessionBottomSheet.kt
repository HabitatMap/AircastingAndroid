package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.AnimatedLoader
import io.lunarlogic.aircasting.lib.ValidationHelper
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.TAGS_SEPARATOR
import kotlinx.android.synthetic.main.edit_session_bottom_sheet.view.*

class EditSessionBottomSheet(private val mListener: Listener,
                             private var mSession: Session,
                             private val mContext: Context?
) : BottomSheetDialogFragment() {
    interface Listener{
        fun onEditDataPressed(session: Session, name: String, tags: ArrayList<String>)
    }

    private var sessionNameInputLayout: TextInputLayout? = null
    private var sessionNameInput: EditText? = null
    private var tagsInput: EditText? = null
    private var mLoader: ImageView? = null
    private val TAG = "EditSessionBottomSheet"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.edit_session_bottom_sheet, container, false)

        mLoader = view?.findViewById(R.id.edit_loader)

        sessionNameInputLayout = view?.findViewById(R.id.session_name)

        sessionNameInput = view?.findViewById<EditText>(R.id.session_name_input)
        sessionNameInput?.setText(mSession.name)

        tagsInput = view?.findViewById<EditText>(R.id.tags_input)
        tagsInput?.setText(mSession.tags.joinToString(TAGS_SEPARATOR))

        val editDataButton = view?.findViewById<Button>(R.id.edit_data_button)
        editDataButton?.setOnClickListener {
            onEditSessionPressed()
        }

        val cancelButton = view?.findViewById<Button>(R.id.cancel_button)
        cancelButton?.setOnClickListener {
            dismiss()
        }

        val closeButton = view?.findViewById<ImageView>(R.id.close_button)
        closeButton?.setOnClickListener {
            dismiss()
        }

        showLoader()

        return view
    }

    fun show(manager: FragmentManager) {
        show(manager, TAG)
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
        val name = view?.session_name_input?.text.toString().trim()
        if (name.isEmpty()) {
            showError()
            return
        }
        val tags = view?.tags_input?.text.toString().trim()
        val tagList = ArrayList(tags.split(TAGS_SEPARATOR))
        dismiss()
        mListener.onEditDataPressed(mSession, name, tagList)
    }

    private fun showError() {
        sessionNameInputLayout?.error = " "
        Toast.makeText(mContext, getString(R.string.session_name_required), Toast.LENGTH_LONG).show()
    }
}
