package pl.llp.aircasting.ui.view.common

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.more_info_bottom_sheet.view.*
import pl.llp.aircasting.R

abstract class BottomSheet : BottomSheetDialogFragment() {
    private val TAG = "BottomSheet"
    protected open val EXPANDED_PERCENT = 0.9
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    protected var contentView: View? = null

    protected abstract fun layoutId(): Int

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
        val cancelButton = contentView?.close_button
        cancelButton?.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun show(manager: FragmentManager) {
        show(manager, TAG)
    }

    protected fun expandBottomSheet(bottomSheetId: Int = R.id.bottomsheet_card) {
        val card = contentView?.findViewById<View>(bottomSheetId) ?: return

        val params = ConstraintLayout.LayoutParams(card.layoutParams)
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        params.height = (screenHeight * EXPANDED_PERCENT).toInt()
        card.layoutParams = params
    }

}
