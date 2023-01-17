package pl.llp.aircasting.ui.view.screens.dashboard

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.confirmation_dialog.view.*
import pl.llp.aircasting.util.extensions.visible

class ConfirmDangerActionDescriptionDialog(
    mFragmentManager: FragmentManager?,
    title: String? = null,
    private val description: String?,
    private val okCallback: () -> (Unit),
) : ConfirmDangerActionDialog(mFragmentManager, title, okCallback) {
    constructor() : this(null, null, null, {})

    private lateinit var mView: View
    override fun setupView(inflater: LayoutInflater): View {
        val view = super.setupView(inflater)

        view.confirmation_dialog_description.apply {
            text = description
            visible()
        }

        return view
    }
}
