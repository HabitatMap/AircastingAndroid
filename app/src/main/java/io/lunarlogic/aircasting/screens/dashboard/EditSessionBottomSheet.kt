package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.events.EditSessionEvent
import io.lunarlogic.aircasting.screens.lets_start.MoreInfoBottomSheet
import kotlinx.android.synthetic.main.edit_session_bottom_sheet.view.*

class EditSessionBottomSheet(private val mListener: Listener, private val sessionId: String): BottomSheetDialogFragment() {
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

        val editDataButton = view?.findViewById<Button>(R.id.edit_data_button)
        editDataButton?.setOnClickListener {
            mListener.onEditDataPressed()
        }

        val cancelButton = view?.findViewById<Button>(R.id.cancel_button)
        cancelButton?.setOnClickListener {
            mListener.onCancelPressed()
        }

        return view
    }

    fun editDataConfirmed(): Triple<String, String, String>{
        val sessionName = view?.session_name_input?.text.toString().trim()
        val tags = view?.tags_input?.text.toString().trim()
        return Triple(sessionId, sessionName, tags)
    }


}
