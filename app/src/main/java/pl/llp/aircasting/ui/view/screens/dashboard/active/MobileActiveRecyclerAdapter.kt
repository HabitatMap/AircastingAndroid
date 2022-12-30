package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsRecyclerAdapter
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel

class MobileActiveRecyclerAdapter(
    private val recyclerView: RecyclerView?,
    private val mInflater: LayoutInflater,
    private val mListener: MobileActiveSessionViewMvc.Listener,
    supportFragmentManager: FragmentManager,
    sessionsViewModel: SessionsViewModel = SessionsViewModel()
) : SessionsRecyclerAdapter<MobileActiveSessionViewMvc.Listener>(
    recyclerView,
    mInflater,
    supportFragmentManager,
    sessionsViewModel
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc =
            MobileActiveSessionViewMvcImpl(
                mInflater,
                parent,
                supportFragmentManager
            )
        viewMvc.registerListener(mListener)
        return MyViewHolder(viewMvc)
    }

    override suspend fun prepareSession(session: Session, expanded: Boolean): Session {
        // We only have to reload measurements for fixed tab for expanded sessions and following tab. Mobile active sessions have measurements fetched anyway
        return session
    }
}
