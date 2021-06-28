package com.caitlykate.bulletinboard.utils

import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.caitlykate.bulletinboard.R

class ItemTouchMoveCallback(val adapter: ItemTouchAdapterInterface): ItemTouchHelper.Callback() {

    override fun getMovementFlags(          //устанавливаем какие именно движения отслеживаем
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlag,0)
    }

    override fun onMove( recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,       //запускается когда двигаем элемент
                         target: RecyclerView.ViewHolder): Boolean {
        //viewHolder - элемент который взяли, target - элемент над которым мы сейчас повисли
        //чтобы указать, что  ItemTouchAdapterInterface.onMove должен запуститься в адаптере, нам нужно его сюда передать
        //точнее интерфейс с этого адаптера
        adapter.onMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {    //когда начинаем удерживать элемент
        //делаем элемент полупрозрачным, когда он не в состоянии покоя (тащим)
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            viewHolder?.itemView?.alpha = 0.5f
            viewHolder?.itemView?.findViewById<TextView>(R.id.tvTitle)?.visibility = View.INVISIBLE
            viewHolder?.itemView?.findViewById<ImageButton>(R.id.imBtDrag)?.visibility = View.INVISIBLE
            viewHolder?.itemView?.findViewById<ImageButton>(R.id.imBtDelete)?.visibility = View.INVISIBLE
            viewHolder?.itemView?.findViewById<LinearLayout>(R.id.underline)?.visibility = View.INVISIBLE
        }

        super.onSelectedChanged(viewHolder, actionState)
    }

    //При перетаскивании элемента onMove() может вызываться более одного раза, но clearView() вызывается один раз.
    // Таким образом, мы можем использовать его, чтобы указать, что перетаскивание закончилось (произошло падение).
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        viewHolder.itemView.alpha = 1.0f
        viewHolder?.itemView?.findViewById<TextView>(R.id.tvTitle)?.visibility = View.VISIBLE
        viewHolder?.itemView?.findViewById<ImageButton>(R.id.imBtDrag)?.visibility = View.VISIBLE
        viewHolder?.itemView?.findViewById<ImageButton>(R.id.imBtDelete)?.visibility = View.VISIBLE
        viewHolder?.itemView?.findViewById<LinearLayout>(R.id.underline)?.visibility = View.VISIBLE
        adapter.onClear()
        super.clearView(recyclerView, viewHolder)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        //не используем
    }

    interface ItemTouchAdapterInterface{
        fun onMove(startPos: Int, targetPos: Int)
        fun onClear()
    }
}