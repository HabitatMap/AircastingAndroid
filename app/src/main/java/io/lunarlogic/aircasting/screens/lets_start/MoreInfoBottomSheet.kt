package io.lunarlogic.aircasting.screens.lets_start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R
import kotlinx.android.synthetic.main.more_info_bottom_sheet.*

class MoreInfoBottomSheet(private val mListener: Listener): BottomSheetDialogFragment() {
    interface Listener {
        fun closePressed()
    }

    private val TAG = "MoreInfoBottomSheet"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.more_info_bottom_sheet, container, false)
        this.isCancelable = false

        val closeButton = view?.findViewById<ImageView>(R.id.close_button)
        closeButton?.setOnClickListener {
            mListener.closePressed()
        }

        return view
    }

    fun show(manager: FragmentManager) {
        show(manager, TAG)
    }
}
