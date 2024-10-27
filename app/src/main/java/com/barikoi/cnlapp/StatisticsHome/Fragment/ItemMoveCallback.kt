package com.barikoi.cnlapp.StatisticsHome.Fragment

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.StatisticsHome.Adapter.TargetAdapter

class ItemMoveCallback : ItemTouchHelper.Callback() {

    private val mAdapter: ItemTouchHelperContract? = null

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {

        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        mAdapter!!.onRowMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true;
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState !== ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is TargetAdapter.ViewHolder) {
                val myViewHolder: TargetAdapter.ViewHolder =
                    viewHolder
                mAdapter!!.onRowSelected(myViewHolder)
            }
        }

        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder is TargetAdapter.ViewHolder) {
            val myViewHolder: TargetAdapter.ViewHolder =
                viewHolder
            mAdapter!!.onRowClear(myViewHolder)
        }
    }
}


interface ItemTouchHelperContract {
    fun onRowMoved(fromPosition: Int, toPosition: Int)
    fun onRowSelected(myViewHolder: TargetAdapter.ViewHolder?)
    fun onRowClear(myViewHolder: TargetAdapter.ViewHolder?)
}