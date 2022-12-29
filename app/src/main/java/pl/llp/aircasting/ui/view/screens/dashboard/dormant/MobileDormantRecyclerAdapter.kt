package pl.llp.aircasting.ui.view.screens.dashboard.dormant

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsRecyclerAdapter


class MobileDormantRecyclerAdapter(
    private val recyclerView: RecyclerView?,
    private val mInflater: LayoutInflater,
    private val mListener: MobileDormantSessionViewMvc.Listener,
    supportFragmentManager: FragmentManager
) : SessionsRecyclerAdapter<MobileDormantSessionViewMvc.Listener>(
    recyclerView,
    mInflater,
    supportFragmentManager
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
