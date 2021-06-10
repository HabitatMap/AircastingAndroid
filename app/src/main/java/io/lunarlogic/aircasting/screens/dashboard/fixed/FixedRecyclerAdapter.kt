package io.lunarlogic.aircasting.screens.dashboard.fixed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class FixedRecyclerAdapter(
    private val mInflater: LayoutInflater,
    private val mListener: FixedSessionViewMvc.Listener,
    supportFragmentManager: FragmentManager
): SessionsRecyclerAdapter<FixedSessionViewMvc.Listener>(mInflater, supportFragmentManager) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc =
            FixedSessionViewMvcImpl(
                mInflater,
                parent,
                supportFragmentManager
            )
        viewMvc.registerListener(mListener)
        return MyViewHolder(viewMvc)
    }

    override fun prepareSession(session: Session, expanded: Boolean): Session {
        if (!expanded) {
            return session
        }

        var reloadedSession: Session? = null
        val job = GlobalScope.launch {
            val dbSessionWithMeasurements = mSessionsViewModel.reloadSessionWithMeasurements(session.uuid)
            dbSessionWithMeasurements?.let {
                reloadedSession = Session(dbSessionWithMeasurements)
            }
        }
        runBlocking {
            job.join()
        }

        return reloadedSession ?: session
    }
}
