package pl.llp.aircasting.ui.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.response.search.SessionInRegionResponse
import pl.llp.aircasting.databinding.ItemSesssionsListFixedFollowBinding
import pl.llp.aircasting.util.disableForASecond

class FixedFollowAdapter constructor(private val onItemClicked: (SessionInRegionResponse) -> Unit) :
    RecyclerView.Adapter<FixedFollowAdapter.DataViewHolder>() {
    private val sessions: ArrayList<SessionInRegionResponse> = ArrayList()
    private var selectedSession: SessionInRegionResponse? = null
    private lateinit var cardView: MaterialCardView

    inner class DataViewHolder(
        private val binding: ItemSesssionsListFixedFollowBinding,
        private val onItemClicked: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(session: SessionInRegionResponse) {
            binding.mySessions = session
            binding.executePendingBindings()

            binding.cvSessions.apply {
                setOnClickListener {
                    onItemClicked(bindingAdapterPosition)
                    disableForASecond()
                }
                cardView = this

                setCorrectLayoutForCard(session)
            }
        }

        private fun setCorrectLayoutForCard(session: SessionInRegionResponse) {
            if (selectedSession?.id == session.id) setBackgroundWithBorder(cardView) else setCardViewToDefault(
                cardView
            )
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

    fun refresh(list: List<SessionInRegionResponse>) {
        sessions.clear()
        sessions.addAll(list)
    }

    fun getSessionPositionBasedOnId(uid: String): Int {
        val session = sessions.first { it.uuid == uid }
        return sessions.indexOf(session)
    }

    fun scrollToSelectedCard(position: Int) {
        setCardViewToDefault(cardView)
        selectedSession = sessions[position]
        notifyItemChanged(position)
    }

    private fun setCardViewToDefault(cardView: MaterialCardView) {
        cardView.apply {
            strokeWidth = 0
            strokeColor = ContextCompat.getColor(context, R.color.aircasting_white)
        }
    }

    private fun setBackgroundWithBorder(cardView: MaterialCardView) {
        cardView.apply {
            strokeWidth = 4
            strokeColor = ContextCompat.getColor(context, R.color.aircasting_blue_400)
        }
    }
}