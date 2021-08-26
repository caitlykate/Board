package com.caitlykate.bulletinboard.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.caitlykate.bulletinboard.MainActivity
import com.caitlykate.bulletinboard.act.EditAdsAct
import com.caitlykate.bulletinboard.model.Ad
import com.caitlykate.bulletinboard.databinding.AdListItemBinding
import com.google.firebase.auth.FirebaseAuth

class AdsRcAdapter(val act: MainActivity): RecyclerView.Adapter<AdsRcAdapter.AdsHolder>() {
    val adArray = ArrayList<Ad>()

    //запускается для каждого объявления и является трудоемкой
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdsHolder {
        //создаем наш вью(клон), а именно ad_list_item
        val binding = AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdsHolder(binding, act )
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
    class AdsHolder(val binding: AdListItemBinding, val act: MainActivity): RecyclerView.ViewHolder(binding.root) {

        fun setData(ad: Ad) = with(binding){

                tvTitle.text = ad.title
                tvDescription.text = ad.description
                tvPrice.text = ad.price + " ₽"

            showEditPanel(isOwner(ad))
            ibEditAd.setOnClickListener(onClickEdit(ad))


        }

        private  fun onClickEdit(ad: Ad): View.OnClickListener{
            return View.OnClickListener {
                val editIntent = Intent(act, EditAdsAct::class.java).apply {
                    putExtra(MainActivity.EDIT_STATE, true)
                    putExtra(MainActivity.ADS_DATA, ad)
                }
                act.startActivity(editIntent)
            }
        }

        private fun isOwner(ad: Ad): Boolean{
            return ad.uid == act.mAuth.uid
        }

        private fun showEditPanel(isOwner: Boolean){
            if(isOwner) binding.editPanel.visibility = View.VISIBLE
            else binding.editPanel.visibility = View.GONE
        }

    }
}