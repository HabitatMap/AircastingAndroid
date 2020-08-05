package io.lunarlogic.aircasting.screens.dashboard.following

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.baoyz.actionsheet.ActionSheet
import io.lunarlogic.aircasting.screens.dashboard.ActiveSessionViewMvcImpl

class FollowingSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    context: Context,
    supportFragmentManager: FragmentManager
):
    ActiveSessionViewMvcImpl<FollowingSessionViewMvc.Listener>(inflater, parent, context, supportFragmentManager),
    FollowingSessionViewMvc {

    override fun onOtherButtonClick(actionSheet: ActionSheet?, index: Int) {
        TODO("Not yet implemented")
    }

    override fun onDismiss(actionSheet: ActionSheet?, isCancel: Boolean) {
        TODO("Not yet implemented")
    }
}
