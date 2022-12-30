package pl.llp.aircasting.ui.view.screens.dashboard.dormant

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsRecyclerAdapter
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel


class MobileDormantRecyclerAdapter(
    private val recyclerView: RecyclerView?,
    private val mInflater: LayoutInflater,
    private val mListener: MobileDormantSessionViewMvc.Listener,
    supportFragmentManager: FragmentManager,
    sessionsViewModel: SessionsViewModel = SessionsViewModel()
) : SessionsRecyclerAdapter<MobileDormantSessionViewMvc.Listener>(
    recyclerView,
    mInflater,
    supportFragmentManager,
    sessionsViewModel
) {

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

    override suspend fun prepareSession(session: Session, expanded: Boolean): Session {
        if (!expanded) {
            return session
        }

        return reloadSessionFromDB(session)
    }
}
