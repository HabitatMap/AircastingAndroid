package pl.llp.aircasting.ui.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.response.search.Session
import pl.llp.aircasting.databinding.ItemSesssionsListFixedFollowBinding

class FixedFollowAdapter constructor(private val onItemClicked: (Session) -> Unit) :
    RecyclerView.Adapter<FixedFollowAdapter.DataViewHolder>() {
    private val sessions: ArrayList<Session> = ArrayList()
    private var selectedSession: Session? = null
    private lateinit var cardView: View

    inner class DataViewHolder(
        private val binding: ItemSesssionsListFixedFollowBinding,
        private val onItemClicked: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(session: Session) {
            binding.mySessions = session
            binding.executePendingBindings()

            binding.root.apply {
                setOnClickListener { onItemClicked(bindingAdapterPosition) }
                cardView = this

                if (selectedSession?.id == session.id) setBackgroundWithBorder(cardView) else setBackgroundWithoutBorder(
                    cardView
                )
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FixedFollowAdapter.DataViewHolder {
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

    fun refresh(list: List<Session>) {
        sessions.clear()
        sessions.addAll(list)
    }

    fun clearAdapter() {
        sessions.clear()
    }

    fun getSessionPositionBasedOnId(uid: String): Int {
        val session = sessions.first { it.uuid == uid }
        return sessions.indexOf(session)
    }

    fun addCardBorder(position: Int) {
        selectedSession = sessions[position]
        removeBorderFromPreviousCard()
        notifyItemChanged(position)
    }

    private fun removeBorderFromPreviousCard() {
        cardView.apply {
            setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.aircasting_white
                )
            )
        }
    }

    private fun setBackgroundWithBorder(cardView: View) {
        cardView.setBackgroundResource(R.drawable.card_view_border_search)
    }

    private fun setBackgroundWithoutBorder(cardView: View) {
        cardView.setBackgroundColor(
            ContextCompat.getColor(
                cardView.context,
                R.color.aircasting_white
            )
        )
    }
}