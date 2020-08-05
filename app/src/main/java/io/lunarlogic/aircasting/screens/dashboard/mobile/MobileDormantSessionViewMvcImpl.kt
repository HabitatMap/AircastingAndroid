package io.lunarlogic.aircasting.screens.dashboard.mobile

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.baoyz.actionsheet.ActionSheet
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvcImpl

class MobileDormantSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    context: Context,
    suppoerFragmentManager: FragmentManager
):
    SessionViewMvcImpl<MobileDormantSessionViewMvc.Listener>(inflater, parent, context, suppoerFragmentManager),
    MobileDormantSessionViewMvc {

    override fun layoutId(): Int {
        return R.layout.dormant_session
    }

    override fun onOtherButtonClick(actionSheet: ActionSheet?, index: Int) {
        TODO("Not yet implemented")
    }

    override fun onDismiss(actionSheet: ActionSheet?, isCancel: Boolean) {
        TODO("Not yet implemented")
    }
}
