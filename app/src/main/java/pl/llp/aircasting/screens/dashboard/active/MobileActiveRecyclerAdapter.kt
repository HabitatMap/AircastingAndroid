package pl.llp.aircasting.screens.dashboard.active

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.dashboard.SessionsRecyclerAdapter

class MobileActiveRecyclerAdapter(
    private val mInflater: LayoutInflater,
    private val mListener: MobileActiveSessionViewMvc.Listener,
    supportFragmentManager: FragmentManager
): SessionsRecyclerAdapter<MobileActiveSessionViewMvc.Listener>(mInflater, supportFragmentManager) {

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

    override fun prepareSession(session: Session, expanded: Boolean): Session {
        // We only have to reload measurements for fixed tab for expanded sessions. Mobile active sessions have measurements fetched anyway
        return session
    }
}
