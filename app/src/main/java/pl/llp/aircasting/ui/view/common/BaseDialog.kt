package pl.llp.aircasting.ui.view.common

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

abstract class BaseDialog(private val mFragmentManager: FragmentManager?): DialogFragment() {
    constructor() : this(null)
    companion object {
        const val TAG = "BaseDialog"
    }

    abstract fun setupView(inflater: LayoutInflater): View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater
            val view = setupView(inflater)
            builder.setView(view)

            val dialog = builder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun show() {
        if (mFragmentManager != null) {
            show(mFragmentManager, TAG)
        }
    }
}
