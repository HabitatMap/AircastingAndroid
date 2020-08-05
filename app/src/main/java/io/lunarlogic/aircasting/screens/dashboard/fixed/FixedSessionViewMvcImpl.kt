package io.lunarlogic.aircasting.screens.dashboard.fixed

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.baoyz.actionsheet.ActionSheet
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvcImpl

class FixedSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    context: Context,
    supportFragmentManager: FragmentManager
):
    SessionViewMvcImpl<FixedSessionViewMvc.Listener>(inflater, parent, context, supportFragmentManager),
    FixedSessionViewMvc {

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
