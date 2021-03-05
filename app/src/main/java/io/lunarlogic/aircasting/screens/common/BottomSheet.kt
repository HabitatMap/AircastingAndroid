package io.lunarlogic.aircasting.screens.common

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.more_info_bottom_sheet.view.*

abstract class BottomSheet: BottomSheetDialogFragment() {
    private val TAG = "BottomSheet"
    private val EXPANDED_PERCENT = 0.9
    protected var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    protected var contentView: View? = null

    abstract protected fun layoutId(): Int

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet = super.onCreateDialog(savedInstanceState)

        contentView = View.inflate(context, layoutId(), null)
        setup()

        val view = contentView ?: return bottomSheet

        bottomSheet.setContentView(view)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)

        return bottomSheet
    }

    protected open fun setup() {
        val cancelButton = contentView?.cancel_button
        cancelButton?.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        bottomSheetBehavior!!.setState(BottomSheetBehavior.STATE_EXPANDED)
    }

    fun show(manager: FragmentManager) {
        show(manager, TAG)
    }

    protected fun expandBottomSheet() {
        val card = contentView?.bottomsheet_card ?: return

        val params = ConstraintLayout.LayoutParams(card.layoutParams)
        params.height = ((Resources.getSystem().getDisplayMetrics().heightPixels) * EXPANDED_PERCENT).toInt()
        card.layoutParams = params
    }
}
