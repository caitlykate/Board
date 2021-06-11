package com.caitlykate.bulletinboard.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.act.EditAdsAct

//передаем контекст EditAdsAct, чтобы можно было поменять там текст в textView
class RcViewDialogSpinnerAdapter(var tvSelection: TextView, var dialog: AlertDialog): RecyclerView.Adapter<RcViewDialogSpinnerAdapter.SpViewHolder>() {
    private val mainList = ArrayList<String>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpViewHolder {       //рисуем 1 элемент и создаем ViewHolder
        val viewItem = LayoutInflater.from(parent.context).inflate(R.layout.sp_list_item,parent,false)
        return SpViewHolder(viewItem, tvSelection, dialog)
    }

    override fun onBindViewHolder(holder: SpViewHolder, position: Int) {                    //подключам к элементам текст
        holder.setData(mainList[position])
    }

    override fun getItemCount(): Int {
        return mainList.size
    }

    class SpViewHolder(itemView: View, var tvSelection: TextView, var dialog: AlertDialog) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var itemText = ""
        fun setData(text:String){                                                           //передаем один город/страну
            val tvSpItem = itemView.findViewById<TextView>(R.id.tvSpItem)                       //находим элемент, куда вставлять текст
            tvSpItem.text = text
            itemText = text
            if (text != "No result") itemView.setOnClickListener(this)


        }

        override fun onClick(v: View?) {
            //(context as EditAdsAct).rootElement.tvCountry.text = itemText             //так можно сделать потому что знаем, что передаем EditAdsAct
                                                                                        //можно tvCountry.setText(itemText)
            tvSelection.setText(itemText)
            dialog.dismiss()
        }
    }

    fun updateAdapter(list: ArrayList<String>){
        mainList.clear()
        mainList.addAll(list)
        notifyDataSetChanged()
    }
}