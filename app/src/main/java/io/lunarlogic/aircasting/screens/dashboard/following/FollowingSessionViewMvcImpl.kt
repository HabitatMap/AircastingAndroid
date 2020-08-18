package io.lunarlogic.aircasting.screens.dashboard.following

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.dashboard.ActiveSessionViewMvcImpl

class FollowingSessionViewMvcImpl:
    ActiveSessionViewMvcImpl<FollowingSessionViewMvc.Listener>,
    FollowingSessionViewMvc {

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup,
        supportFragmentManager: FragmentManager
    ): super(inflater, parent, supportFragmentManager) {
        val actionsView = this.rootView?.findViewById<ImageView>(R.id.session_actions_button)
        actionsView?.visibility = View.GONE
    }

    override fun layoutId(): Int {
        return R.layout.active_session
    }

    override fun buildBottomSheet(): BottomSheet? {
        return null;
    }
}
