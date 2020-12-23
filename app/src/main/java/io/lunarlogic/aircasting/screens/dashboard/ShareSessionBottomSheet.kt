package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Session

class ShareSessionBottomSheet(private val mListener: ShareSessionBottomSheet.Listener, private val session: Session): BottomSheetDialogFragment() {
    interface Listener{
        fun onShareLinkPressed()
        fun onShareFilePressed()
        fun onCancelPressed()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.share_session_bottom_sheet, container, false)

        val emailInput = view?.findViewById<EditText>(R.id.email_input)

        val shareLinkButton = view?.findViewById<Button>(R.id.share_link_button)
        shareLinkButton?.setOnClickListener {
            mListener.onShareLinkPressed()
        }

        val shareFileButton = view?.findViewById<Button>(R.id.share_file_button)
        shareFileButton?.setOnClickListener {
            mListener.onShareFilePressed()
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


}
