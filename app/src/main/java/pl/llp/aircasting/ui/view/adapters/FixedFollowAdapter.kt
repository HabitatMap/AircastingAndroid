package pl.llp.aircasting.ui.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.llp.aircasting.data.api.responses.search.Session
import pl.llp.aircasting.databinding.ItemSesssionsListFixedFollowBinding

class FixedFollowAdapter(private val sessions: ArrayList<Session>) :
    RecyclerView.Adapter<FixedFollowAdapter.DataViewHolder>() {

    inner class DataViewHolder(private val binding: ItemSesssionsListFixedFollowBinding) :
        RecyclerView.ViewHolder(binding.root) {
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
        return DataViewHolder(binding)
    }

    override fun getItemCount(): Int = sessions.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) =
        holder.bind(sessions[position])

    fun addData(list: List<Session>) {
        sessions.addAll(list)
    }

}