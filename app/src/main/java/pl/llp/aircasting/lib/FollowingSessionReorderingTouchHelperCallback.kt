package pl.llp.aircasting.lib

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder



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
    
    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        mAdapter?.onItemDismiss(viewHolder.adapterPosition)
    }

    init {
        mAdapter = itemTouchHelperAdapter
    }
}
