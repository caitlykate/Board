package com.caitlykate.bulletinboard.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.utils.CityHelper


class DialogSpinnerHelper {
    fun showSpinnerDialog(context: Context, list: ArrayList<String>, tvSelection: TextView){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        //в диалог передаем разметку
        val rootView = LayoutInflater.from(context).inflate(R.layout.spinner_layout,null)     //в обычном классе нет контекста, берем из EditAdsAct
        val adapter = RcViewDialogSpinnerAdapter(tvSelection,dialog)
        val rcView = rootView.findViewById<RecyclerView>(R.id.rcSpView)
        val searchView = rootView.findViewById<SearchView>(R.id.svSpinner)
        rcView.layoutManager = LinearLayoutManager(context)                                        //устанавливаем как будет выглядеть (как простой список по вертикали)
        rcView.adapter = adapter
        adapter.updateAdapter(list)
        dialog.setView(rootView)
        setSearchViewListener(adapter, list, searchView)
        dialog.show()
    }

    private fun setSearchViewListener(adapter: RcViewDialogSpinnerAdapter, list: ArrayList<String>, searchView: SearchView?) {
        //add to sv listener of text update
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val newList = CityHelper.filterListData(list, newText)
                adapter.updateAdapter(newList)
                return true
            }
        })

    }

    //private fun setSearchViewListener(adapter: RcViewDialogSpinnerAdapter, )
}