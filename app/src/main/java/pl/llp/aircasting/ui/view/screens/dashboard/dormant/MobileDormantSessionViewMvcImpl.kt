package pl.llp.aircasting.ui.view.screens.dashboard.dormant

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.dashboard.SessionViewMvcImpl
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.session_actions.mobile.dormant.MobileDormantSessionActionsBottomSheet

class MobileDormantSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    supporFragmentManager: FragmentManager
):
    SessionViewMvcImpl<MobileDormantSessionViewMvc.Listener>(inflater, parent, supporFragmentManager),
    MobileDormantSessionViewMvc,
    MobileDormantSessionActionsBottomSheet.Listener
{
    override fun showMeasurementsTableValues(): Boolean {
        return false
    }

    override fun bindMeasurementsTable() {
        super.bindMeasurementsTable()
    }

    override fun showExpandedMeasurementsTableValues() = true

    override fun bindCollapsedMeasurementsDescription() {
        mMeasurementsDescription?.text = context.getString(R.string.parameters)
    }

    override fun bindExpandedMeasurementsDescription() {
        mMeasurementsDescription?.text = context.getString(R.string.session_avg_measurements_description)
    }

    override fun editSessionPressed() {
        val session = mSessionPresenter?.session ?: return

        for(listener in listeners) {
            listener.onSessionEditClicked(session)
        }
        dismissBottomSheet()
    }

    override fun shareSessionPressed() {
        val session = mSessionPresenter?.session ?: return

        for(listener in listeners){
            listener.onSessionShareClicked(session)
        }
        dismissBottomSheet()
    }

    override fun deleteSessionPressed() {
        val session = mSessionPresenter?.session ?: return

        for (listener in listeners) {
            listener.onSessionDeleteClicked(session)
        }
        dismissBottomSheet()
    }
}
