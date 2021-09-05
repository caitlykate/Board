package com.caitlykate.bulletinboard.adapters

import androidx.recyclerview.widget.DiffUtil
import com.caitlykate.bulletinboard.model.Ad

class DiffUtilHelper(val oldList: List<Ad>, val newList: List<Ad>): DiffUtil.Callback() {
    //все эти ф-ии определяют что делать с новым списком , в зависимости от того, что было в старом списке

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        //нужно ли перерисовывать
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList == newList
    }
}