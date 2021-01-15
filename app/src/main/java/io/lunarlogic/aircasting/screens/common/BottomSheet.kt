package io.lunarlogic.aircasting.screens.common

import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.AnimatedLoader


abstract class BottomSheet(private val mListener: Listener): BottomSheetDialogFragment() {
    interface Listener {
        fun cancelPressed()
    }

    private val TAG = "BottomSheet"

    abstract protected fun layoutId(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(layoutId(), container, false)

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
