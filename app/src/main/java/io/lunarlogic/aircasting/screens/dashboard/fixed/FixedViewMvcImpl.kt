package io.lunarlogic.aircasting.screens.dashboard.fixed

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvcImpl
import io.lunarlogic.aircasting.models.Session


class FixedViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
): SessionsViewMvcImpl<FixedSessionViewMvc.Listener>(inflater, parent, supportFragmentManager),
    FixedSessionViewMvc.Listener {

    override fun buildAdapter(
        inflater: LayoutInflater,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<FixedSessionViewMvc.Listener> {
        return FixedRecyclerAdapter(
            inflater,
            this,
            supportFragmentManager
        )
    }

    override fun onSessionEditClicked(session: Session) {
        for (listener in listeners) {
            Log.i("FIXED_IMPL", "Edit session clicked")
            listener.onEditSessionClicked(session)
        }
    }

    override fun onSessionDeleteClicked(session: Session) {
        for (listener in listeners) {
            listener.onDeleteSessionClicked(session.uuid)
        }
    }
}
