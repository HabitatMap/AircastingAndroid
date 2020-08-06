package io.lunarlogic.aircasting.screens.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R


class BottomSheet: BottomSheetDialogFragment() {
    private val TAG = "BottomSheet"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dormant_session_actions, container, false)
    }

    fun show(manager: FragmentManager) {
        show(manager, TAG)
    }
}
