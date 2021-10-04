package com.caitlykate.bulletinboard.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.caitlykate.bulletinboard.MainActivity
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.act.DescriptionActivity
import com.caitlykate.bulletinboard.act.EditAdsAct
import com.caitlykate.bulletinboard.model.Ad
import com.caitlykate.bulletinboard.databinding.AdListItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

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
        val tempArray = ArrayList<Ad>()
        tempArray.addAll(adArray)
        tempArray.addAll(newList)
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray,tempArray))
        diffResult.dispatchUpdatesTo(this)
        adArray.clear()
        adArray.addAll(tempArray)
    }

    fun updateAdapterWithClear(newList: List<Ad>){
        //определяем похожи ли элементы и какую ф-ю применить
        //например notifyDataSetChanged()/notifyItemRangeChanged()
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray,newList))
        diffResult.dispatchUpdatesTo(this)  //применить наши изменения (анимация)
        adArray.clear()
        adArray.addAll(newList)
        //notifyDataSetChanged()          //не рисуем заново вью, а перезаполняем имеющиеся новыми данными

    }

    //отвечает за одно объявление
    //каждый раз, когда создается новый элемент - создается класс AdHolder
    //при скролле назад объявления не создаются заново, а берутся из памяти (из AdHolder)
    class AdsHolder(val binding: AdListItemBinding, val act: MainActivity) :
        RecyclerView.ViewHolder(binding.root) {

        //здесь заполняется
        fun setData(ad: Ad) = with(binding) {

            tvTitle.text = ad.title
            tvDescription.text = ad.description
            tvPrice.text = ad.price + " ₽"
            tvViewCounter.text = ad.viewsCounter
            tvFavCounter.text = ad.favCounter
            Picasso.get().load(ad.mainImage).into(mainImage)
            isFav(ad)
            showEditPanel(isOwner(ad))
            mainOnClick(ad)
        }

        private fun isFav(ad: Ad){
            if (ad.isFav) {
                binding.ibFav.setImageResource(R.drawable.ic_fav_pressed)
            } else {
                binding.ibFav.setImageResource(R.drawable.ic_fav_normal)
            }
        }

        private fun mainOnClick(ad: Ad) = with(binding){
            //при нажатии на весь элемент открываем DescriptionActivity и добавляем +1 просмотр
            itemView.setOnClickListener {
                act.onAdViewed(ad)
            }
            ibFav.setOnClickListener {
                if (act.mAuth.currentUser?.isAnonymous == false) act.onFavClicked(ad)
            }
            ibEditAd.setOnClickListener(onClickEdit(ad))
            ibDeleteAd.setOnClickListener {
                act.onDeleteItem(ad)
            }
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

    //и-с, который будем запускать для того, чтобы при нажатии на кнопку удалить,
    // у нас этот интерфейс запустился на mainAct и там уже мы запускаем ф-ии для удаления из базы и адаптера
    interface Listener{
        //можно было реализовать с помощью одной функции и уже в MainAct проверять на что мы нажали
        fun onDeleteItem(ad: Ad)
        fun onAdViewed(ad: Ad)
        fun onFavClicked(ad: Ad)
    }
}