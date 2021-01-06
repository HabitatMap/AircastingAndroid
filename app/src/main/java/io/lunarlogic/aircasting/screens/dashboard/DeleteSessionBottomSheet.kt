package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Session

class DeleteSessionBottomSheet(private val mListener: DeleteSessionBottomSheet.Listener, private val session: Session): BottomSheetDialogFragment() {
    interface Listener {
        fun onDeleteStreamsPressed()
        fun onCancelDeleteSessionDialogPressed()
    }
    private var mStreamsOptionsContainer: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.delete_session_bottom_sheet, container, false)
        val deleteStreamsButton = view?.findViewById<Button>(R.id.delete_streams_button)
        val cancelButton = view?.findViewById<Button>(R.id.cancel_button)
        val closeButton = view?.findViewById<ImageView>(R.id.close_button)

        cancelButton?.setOnClickListener {
            mListener.onCancelDeleteSessionDialogPressed()
        }

        closeButton?.setOnClickListener {
            mListener.onCancelDeleteSessionDialogPressed()
        }

        deleteStreamsButton?.setOnClickListener {
            mListener.onDeleteStreamsPressed()
        }

        mStreamsOptionsContainer = view?.findViewById(R.id.streams_options_container)
        // TODO: generate checkboxes with streams
        return view
    }
}
