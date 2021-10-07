package pl.llp.aircasting.screens.dashboard.reordering_following

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.lib.ItemTouchHelperAdapter
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.dashboard.SessionCardListener
import pl.llp.aircasting.screens.dashboard.following.FollowingRecyclerAdapter
import pl.llp.aircasting.screens.dashboard.following.FollowingSessionViewMvcImpl
import java.util.*

class ReorderingFollowingRecyclerAdapter (
    private val mInflater: LayoutInflater,
    private val mListener: SessionCardListener,
    supportFragmentManager: FragmentManager
): FollowingRecyclerAdapter(mInflater, mListener, supportFragmentManager),
    ItemTouchHelperAdapter {

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

    override fun onItemMove(fromPosition: Int, toPosition: Int) { // TODO: somehow i have to pass mSessionUUIDS list back to SessionsRecyclerADapter or somewhere?!?!
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(mSessionUUIDS, i, i + 1)
                DatabaseProvider.runQuery {
                    mSessionsViewModel.updateOrder(mSessionUUIDS[i+1], i+1)
                    mSessionsViewModel.updateOrder(mSessionUUIDS[i], i)
                }
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(mSessionUUIDS, i, i - 1)
                DatabaseProvider.runQuery {
                    mSessionsViewModel.updateOrder(mSessionUUIDS[i-1], i-1)
                    mSessionsViewModel.updateOrder(mSessionUUIDS[i], i)
                }
            }
        }

        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int) {
        Log.i("LIST", "list size: " + mSessionUUIDS.size.toString())
        mSessionUUIDS.removeAt(position)
        Log.i("LIST", "list size: " + mSessionUUIDS.size.toString())
        //TODO: i have to update order of all sessions below removed one too!!!!
        for (session in mSessionUUIDS) {
            DatabaseProvider.runQuery {
                mSessionsViewModel.updateOrder(session, mSessionUUIDS.indexOf(session)) //todo: not sure if thats correct
            }
        }
        removeObsoleteSessions()
        notifyItemRemoved(position)
    }

    override fun removeObsoleteSessions() {
        mSessionPresenters.keys
            .filter { uuid -> !mSessionUUIDS.contains(uuid) }
            .forEach { uuid ->
                val sessionPresenter = mSessionPresenters[uuid]
                sessionPresenter?.session?.unfollow()
                if (sessionPresenter?.session != null) {
                    DatabaseProvider.runQuery {
                        mSessionsViewModel.updateFollowedAt(sessionPresenter.session!!)
                    }
                }
                mSessionPresenters.remove(uuid)
            }
    }

}
