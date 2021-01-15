package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.AnimatedLoader
import io.lunarlogic.aircasting.screens.common.BottomSheet

class SessionActionsBottomSheet(private val mListener: Listener): BottomSheet(mListener) {
    interface Listener: BottomSheet.Listener {
        fun editSessionPressed()
        fun shareSessionPressed()
        fun deleteSessionPressed()
    }

    var mLoader: ImageView? = null

    override fun layoutId(): Int {
        return R.layout.session_actions;
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        mLoader = view?.findViewById(R.id.loader)

        val editButton = view?.findViewById<Button>(R.id.edit_session_button)
        editButton?.setOnClickListener {
            mListener.editSessionPressed()
        }

        val shareButton = view?.findViewById<Button>(R.id.share_session_button)
        shareButton?.setOnClickListener {
            mListener.shareSessionPressed()
        }

        val deleteButton = view?.findViewById<Button>(R.id.delete_session_button)
        deleteButton?.setOnClickListener {
            mListener.deleteSessionPressed()
        }

        return view
    }

    fun showLoader(){ //todo : trial for now
        AnimatedLoader(mLoader).start()
        mLoader?.visibility = View.VISIBLE
    }

    fun hideLoader(){ //todo : trial for now
        mLoader?.visibility = View.GONE
    }
}
