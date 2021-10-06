package pl.llp.aircasting.screens.dashboard.following

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.lib.ItemTouchHelperAdapter
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.dashboard.SessionCardListener
import pl.llp.aircasting.screens.dashboard.SessionsRecyclerAdapter
import java.util.*


open class FollowingRecyclerAdapter(
    private val mInflater: LayoutInflater,
    private val mListener: SessionCardListener,
    supportFragmentManager: FragmentManager
): SessionsRecyclerAdapter<SessionCardListener>(mInflater, supportFragmentManager) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc =
            FollowingSessionViewMvcImpl(
                mInflater,
                parent,
                supportFragmentManager
            )
        viewMvc.registerListener(mListener)
        return MyViewHolder(viewMvc)
    }

    override fun prepareSession(session: Session, expanded: Boolean): Session {
        // We only have to reload measurements for fixed tab for expanded sessions. Followed sessions have measurements fetched anyway
        return session
    }

}
