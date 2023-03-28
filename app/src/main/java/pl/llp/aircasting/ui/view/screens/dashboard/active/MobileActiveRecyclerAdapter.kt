package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsRecyclerAdapter

class MobileActiveRecyclerAdapter(
    private val recyclerView: RecyclerView?,
    private val mInflater: LayoutInflater,
    private val mListener: MobileActiveSessionViewMvc.Listener,
    supportFragmentManager: FragmentManager,
    reloadSessionCallback: suspend (uuid: String) -> SessionWithStreamsAndMeasurementsDBObject?,
) : SessionsRecyclerAdapter<MobileActiveSessionViewMvc.Listener>(
    recyclerView,
    mInflater,
    supportFragmentManager,
    reloadSessionCallback
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
