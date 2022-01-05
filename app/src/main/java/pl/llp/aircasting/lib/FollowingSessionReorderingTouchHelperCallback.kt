package pl.llp.aircasting.lib

<<<<<<< HEAD
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import pl.llp.aircasting.R
=======
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder


>>>>>>> 921b2413 (rebase 3)


class FollowingSessionReorderingTouchHelperCallback(itemTouchHelperAdapter: ItemTouchHelperAdapter) :
    ItemTouchHelper.Callback() {

    private var mAdapter: ItemTouchHelperAdapter? = itemTouchHelperAdapter

    override fun isLongPressDragEnabled(): Boolean {
<<<<<<< HEAD
        return false
=======
        return true
>>>>>>> 921b2413 (rebase 3)
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
<<<<<<< HEAD

=======
>>>>>>> 921b2413 (rebase 3)
        return true
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        mAdapter?.onItemDismiss(viewHolder.adapterPosition)
    }

    init {
        mAdapter = itemTouchHelperAdapter
    }
}
<<<<<<< HEAD

=======
>>>>>>> 921b2413 (rebase 3)
