package io.lunarlogic.aircasting.screens.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R


open class BottomSheet(private val mListener: Listener): BottomSheetDialogFragment() {
    interface Listener {
        fun cancelPressed()
    }

    private val TAG = "BottomSheet"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dormant_session_actions, container, false)

        val cancelButton = view.findViewById<Button>(R.id.cancel_button)
        cancelButton.setOnClickListener {
            mListener.cancelPressed()
        }

        return view;
    }

    fun show(manager: FragmentManager) {
        show(manager, TAG)
    }
}
