package io.lunarlogic.aircasting.screens.dashboard.mobile

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.dashboard.SessionActionsBottomSheet
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvcImpl
import kotlinx.android.synthetic.main.session_card.view.*

class MobileDormantSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    supporFragmentManager: FragmentManager
):
    SessionViewMvcImpl<MobileDormantSessionViewMvc.Listener>(inflater, parent, supporFragmentManager),
    MobileDormantSessionViewMvc,
    SessionActionsBottomSheet.Listener
{
    private val mMeasurementsDescriptionTextView: TextView?

    init {
        mMeasurementsDescriptionTextView = this.rootView?.session_measurements_description
        setCollapsedMeasurementDescriptionText()
    }

    override fun showMeasurementsTableValues(): Boolean {
        return false
    }

    override fun buildBottomSheet(): BottomSheet {
        return SessionActionsBottomSheet(this)
    }

    override fun deleteSessionPressed() {
        for (listener in listeners) {
            listener.onSessionDeleteClicked(mSessionPresenter!!.session!!)
        }
        dismissBottomSheet()
    }

    override fun expandSessionCard() {
        super.expandSessionCard()
        setExpandedMeasurementDescriptionText()
    }

    override fun collapseSessionCard() {
        super.collapseSessionCard()
        setCollapsedMeasurementDescriptionText()
    }

    private fun setCollapsedMeasurementDescriptionText() {
        mMeasurementsDescriptionTextView?.text = context.getString(R.string.parameters)
    }

    private fun setExpandedMeasurementDescriptionText() {
        mMeasurementsDescriptionTextView?.text = context.getString(R.string.session_measurements_description)
    }

    override fun chartUnitLabelId(): Int {
        return R.string.mobile_session_units_label
    }
}
