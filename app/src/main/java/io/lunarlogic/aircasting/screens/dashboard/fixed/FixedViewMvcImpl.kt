package io.lunarlogic.aircasting.screens.dashboard.fixed

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvcImpl
import io.lunarlogic.aircasting.sensor.Session


class FixedViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    context: Context,
    supportFragmentManager: FragmentManager
): SessionsViewMvcImpl<FixedSessionViewMvc.Listener>(inflater, parent, context, supportFragmentManager),
    FixedSessionViewMvc.Listener {

    override fun buildAdapter(
        inflater: LayoutInflater,
        context: Context,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<FixedSessionViewMvc.Listener> {
        return FixedRecyclerAdapter(
            inflater,
            this,
            context,
            supportFragmentManager
        )
    }

    override fun onSessionDeleteClicked(session: Session) {
        for (listener in listeners) {
            listener.onDeleteSessionClicked(session.uuid)
        }
    }
}
