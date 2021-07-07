package pl.llp.aircasting.screens.dashboard.fixed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.dashboard.SessionsRecyclerAdapter
import kotlinx.coroutines.*


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

}
