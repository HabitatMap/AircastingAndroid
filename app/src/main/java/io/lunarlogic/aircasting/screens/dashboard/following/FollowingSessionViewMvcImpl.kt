package io.lunarlogic.aircasting.screens.dashboard.following

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.dashboard.ActiveSessionViewMvcImpl

class FollowingSessionViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup,
    supportFragmentManager: FragmentManager
):
    ActiveSessionViewMvcImpl<FollowingSessionViewMvc.Listener>(inflater, parent, supportFragmentManager),
    FollowingSessionViewMvc {
    override fun buildBottomSheet(): BottomSheet? {
        return null;
    }
}
