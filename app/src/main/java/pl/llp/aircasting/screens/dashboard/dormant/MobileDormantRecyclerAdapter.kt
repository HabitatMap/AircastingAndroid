package pl.llp.aircasting.screens.dashboard.dormant

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.dashboard.SessionsRecyclerAdapter


class MobileDormantRecyclerAdapter(
    private val mInflater: LayoutInflater,
    private val mListener: MobileDormantSessionViewMvc.Listener,
    supportFragmentManager: FragmentManager
): SessionsRecyclerAdapter<MobileDormantSessionViewMvc.Listener>(mInflater, supportFragmentManager) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc =
            MobileDormantSessionViewMvcImpl(
                mInflater,
                parent,
                supportFragmentManager
            )
        viewMvc.registerListener(mListener)
        return MyViewHolder(viewMvc)
    }

    override fun prepareSession(session: Session, expanded: Boolean): Session {
//        if (!expanded) {
//            return session
//        }
//
//        var reloadedSession: Session? = null
//
//        runBlocking {
//            val query = GlobalScope.async(Dispatchers.IO) {
//                val dbSessionWithMeasurements = mSessionsViewModel.reloadSessionWithMeasurements(session.uuid)
//                dbSessionWithMeasurements?.let {
//                    reloadedSession = Session(dbSessionWithMeasurements)
//                }
//            }
//            query.await()
//        }
//
//        return reloadedSession ?: session

        // We only have to reload measurements for fixed tab for expanded sessions. Mobile dormant sessions are not reloaded anymore
        return session
    }
}
