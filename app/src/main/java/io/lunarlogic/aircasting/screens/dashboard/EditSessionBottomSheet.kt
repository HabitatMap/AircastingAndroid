package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.database.converters.TagsConverter
import io.lunarlogic.aircasting.events.EditSessionEvent
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.TAGS_SEPARATOR
import io.lunarlogic.aircasting.screens.lets_start.MoreInfoBottomSheet
import kotlinx.android.synthetic.main.edit_session_bottom_sheet.view.*

class EditSessionBottomSheet(private val mListener: Listener, private val session: Session): BottomSheetDialogFragment() {
    interface Listener{
        fun onEditDataPressed()
        fun onCancelPressed()
    }

    private val TAG = "EditSessionBottomSheet"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.edit_session_bottom_sheet, container, false)

        val sessionNameInput = view?.findViewById<EditText>(R.id.session_name_input)
        sessionNameInput?.setText(session.name)

        val tagsInput = view?.findViewById<EditText>(R.id.tags_input)
        tagsInput?.setText(prepairTagsString(session.tags.toString()))

        val editDataButton = view?.findViewById<Button>(R.id.edit_data_button)
        editDataButton?.setOnClickListener {
            mListener.onEditDataPressed()
        }

        val cancelButton = view?.findViewById<Button>(R.id.cancel_button)
        cancelButton?.setOnClickListener {
            mListener.onCancelPressed()
        }

        val closeButton = view?.findViewById<ImageView>(R.id.close_button)
        closeButton?.setOnClickListener {
            mListener.onCancelPressed()
        }

        return view
    }

    fun editDataConfirmed(): Session{
        val sessionName = view?.session_name_input?.text.toString().trim()
        val tags = view?.tags_input?.text.toString().trim()
        val tag_list = getSessionTags(tags)
        session.sessionEdited(sessionName, tag_list)
        return session
    }

    private fun getSessionTags(tags: String): ArrayList<String> {
        return ArrayList(tags.split(TAGS_SEPARATOR))
    }

    private fun prepairTagsString(listString: String): String{
        var listStringNew = listString.replace("[", "")
        listStringNew = listStringNew.replace("]", "")
        listStringNew = listStringNew.replace(",", "")
        return listStringNew
    }

}
