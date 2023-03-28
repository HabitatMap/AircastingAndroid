package pl.llp.aircasting.ui.view.screens.dashboard.fixed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsRecyclerAdapter


open class FixedRecyclerAdapter<ListenerType : FixedSessionViewMvc.Listener>(
    private val recyclerView: RecyclerView?,
    private val mInflater: LayoutInflater,
    private val mListener: FixedSessionViewMvc.Listener,
    supportFragmentManager: FragmentManager,
    reloadSessionCallback: suspend (uuid: String) -> SessionWithStreamsAndMeasurementsDBObject?,
) : SessionsRecyclerAdapter<FixedSessionViewMvc.Listener>(
    recyclerView,
    mInflater,
    supportFragmentManager,
    reloadSessionCallback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc =
            FixedSessionViewMvcImpl<FixedSessionViewMvc.Listener>(
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
