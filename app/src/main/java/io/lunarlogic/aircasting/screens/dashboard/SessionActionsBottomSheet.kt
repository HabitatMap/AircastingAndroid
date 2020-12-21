package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BottomSheet

class SessionActionsBottomSheet(private val mListener: Listener): BottomSheet(mListener) {
    interface Listener: BottomSheet.Listener {
        fun editSessionPressed()
        fun deleteSessionPressed()
    }

    override fun layoutId(): Int {
        return R.layout.session_actions;
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val editButton = view?.findViewById<Button>(R.id.edit_session_button)
        editButton?.setOnClickListener {
            Log.i("BOTTOM_SHEET", "Edit clicked")
            mListener.editSessionPressed()
        }

        val deleteButton = view?.findViewById<Button>(R.id.delete_session_button)
        deleteButton?.setOnClickListener {
            mListener.deleteSessionPressed()
        }

        return view
    }
}
