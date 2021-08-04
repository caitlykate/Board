package com.caitlykate.bulletinboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.caitlykate.bulletinboard.data.Ad
import com.caitlykate.bulletinboard.databinding.AdListItemBinding
import com.google.firebase.auth.FirebaseAuth

class AdsRcAdapter(val auth: FirebaseAuth): RecyclerView.Adapter<AdsRcAdapter.AdsHolder>() {
    val adArray = ArrayList<Ad>()

    //запускается для каждого объявления и является трудоемкой
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdsHolder {
        //создаем наш вью(клон), а именно ad_list_item
        val binding = AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdsHolder(binding, auth )
    }

    //когда holder создан и подключен к нашему вью - заполняем данными из массива
    override fun onBindViewHolder(holder: AdsHolder, position: Int) {
        holder.setData(adArray[position])
    }

    override fun getItemCount(): Int {
        return adArray.size
    }

    fun updateAdapter(newList: List<Ad>){
        adArray.clear()
        adArray.addAll(newList)
        notifyDataSetChanged()          //не рисуем заново вью, а перезаполняем имеющиеся новыми данными
    }

    //каждый раз, когда создается новый элемент - создается класс AdHolder
    //при скролле назад объявления не создаются заново, а берутся из памяти (из AdHolder)
    class AdsHolder(val binding: AdListItemBinding, val auth: FirebaseAuth): RecyclerView.ViewHolder(binding.root) {

        fun setData(ad: Ad){
            binding.apply{
                tvTitle.text = ad.title
                tvDescription.text = ad.description
                tvPrice.text = ad.price + " ₽"
            }
            showEditPanel(isOwner(ad))

        }

        private fun isOwner(ad: Ad): Boolean{
            return ad.uid == auth.uid
        }

        private fun showEditPanel(isOwner: Boolean){
            if(isOwner) binding.editPanel.visibility = View.VISIBLE
            else binding.editPanel.visibility = View.GONE
        }

    }
}