package pl.llp.aircasting.lib

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import pl.llp.aircasting.R


class FollowingSessionReorderingTouchHelperCallback(itemTouchHelperAdapter: ItemTouchHelperAdapter) :
    ItemTouchHelper.Callback() {

    private var mAdapter: ItemTouchHelperAdapter? = itemTouchHelperAdapter

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView, viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        mAdapter?.onItemMove(viewHolder.adapterPosition, target.adapterPosition)

        return true
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        hideReorderInProgressIcon(viewHolder)
    }

    override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            showReorderInProgressIcon(viewHolder)
        }

    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        mAdapter?.onItemDismiss(viewHolder.adapterPosition)
    }

    private fun showReorderInProgressIcon(viewHolder: ViewHolder?) {
        viewHolder?.itemView?.findViewById<ImageView>(R.id.reorder_session_button)?.visibility = View.INVISIBLE
        viewHolder?.itemView?.findViewById<ImageView>(R.id.reorder_inprogress_session_button)?.visibility = View.VISIBLE
    }

    private fun hideReorderInProgressIcon(viewHolder: ViewHolder) {
        viewHolder.itemView.findViewById<ImageView>(R.id.reorder_session_button)?.visibility = View.VISIBLE
        viewHolder.itemView.findViewById<ImageView>(R.id.reorder_inprogress_session_button)?.visibility = View.INVISIBLE
    }

    init {
        mAdapter = itemTouchHelperAdapter
    }
}

