package pl.llp.aircasting.ui.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.llp.aircasting.data.api.responses.search.Session
import pl.llp.aircasting.databinding.ItemSesssionsListFixedFollowBinding

class FixedFollowAdapter(
    private val sessions: ArrayList<Session>,
    private val onItemClicked: (Session) -> Unit
) :
    RecyclerView.Adapter<FixedFollowAdapter.DataViewHolder>() {

    inner class DataViewHolder(
        private val binding: ItemSesssionsListFixedFollowBinding,
        private val onItemClicked: (Int) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onItemClicked(bindingAdapterPosition)
            }
        }

        fun bind(sessions: Session) {
            binding.mySessions = sessions
            binding.executePendingBindings()
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
}