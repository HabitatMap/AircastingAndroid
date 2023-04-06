package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.edit

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.edit_session_bottom_sheet.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.TAGS_SEPARATOR
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.util.events.UpdateSessionEvent
import pl.llp.aircasting.util.extensions.showToast
import pl.llp.aircasting.util.extensions.startAnimation
import pl.llp.aircasting.util.extensions.stopAnimation
import javax.inject.Inject

class EditSessionBottomSheet(
    private var mSession: Session?
) : BottomSheet() {
    constructor() : this(null)

    private var sessionNameInputLayout: TextInputLayout? = null
    private var sessionNameInput: EditText? = null
    private var tagsInput: EditText? = null
    private var mLoader: ImageView? = null

    @Inject
    lateinit var viewModel: EditSessionBottomSheetViewModel

    override fun layoutId(): Int {
        return R.layout.edit_session_bottom_sheet
    }

    override fun setup() {
        (activity?.application as AircastingApplication).userDependentComponent?.inject(this)

        mLoader = contentView?.edit_loader

        sessionNameInputLayout = contentView?.session_name

        sessionNameInput = contentView?.session_name_input
        sessionNameInput?.setText(mSession?.name)

        tagsInput = contentView?.tags_input
        tagsInput?.setText(mSession?.tags?.joinToString(TAGS_SEPARATOR))

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
        lifecycleScope.launch {
            viewModel.reload(mSession)
                .flowOn(Dispatchers.IO)
                .collect {
                    it.onSuccess { session ->
                        reload(session)
                    }
                    hideLoader()
                }
        }
    }

    private fun reload(session: Session) {
        mSession = session
        sessionNameInput?.setText(mSession?.name)
        tagsInput?.setText(mSession?.tags?.joinToString(TAGS_SEPARATOR))
    }

    private fun showLoader() {
        mLoader?.startAnimation()
        mLoader?.visibility = View.VISIBLE
        sessionNameInput?.isEnabled = false
        tagsInput?.isEnabled = false
    }

    private fun hideLoader() {
        mLoader?.stopAnimation()
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

        mSession?.let {
            EventBus.getDefault().post(UpdateSessionEvent(it, name, tagList))
        }
        dismiss()
    }

    private fun showError() {
        sessionNameInputLayout?.error = " "
        requireActivity().showToast(getString(R.string.session_name_required), Toast.LENGTH_LONG)
    }
}
