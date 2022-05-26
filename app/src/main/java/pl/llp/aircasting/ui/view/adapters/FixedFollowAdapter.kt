package pl.llp.aircasting.ui.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.llp.aircasting.data.api.response.search.Session
import pl.llp.aircasting.databinding.ItemSesssionsListFixedFollowBinding

class FixedFollowAdapter constructor(private val onItemClicked: (Session) -> Unit) :
    RecyclerView.Adapter<FixedFollowAdapter.DataViewHolder>() {
    private val sessions: ArrayList<Session> = ArrayList()

    inner class DataViewHolder(
        private val binding: ItemSesssionsListFixedFollowBinding,
        private val onItemClicked: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sessions: Session) {
            binding.mySessions = sessions
            binding.executePendingBindings()

            binding.root.setOnClickListener { onItemClicked(bindingAdapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val binding = ItemSesssionsListFixedFollowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DataViewHolder(binding) {
            onItemClicked(sessions[it])
        }
    }

    override fun getItemCount(): Int = sessions.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) =
        holder.bind(sessions[position])

    fun addData(list: List<Session>) {
        sessions.addAll(list)
    }

    fun getSessionPositionBasedOnId(uid: String): Int {
        val session = sessions.first { it.uuid == uid }
        return sessions.indexOf(session)
    }
}