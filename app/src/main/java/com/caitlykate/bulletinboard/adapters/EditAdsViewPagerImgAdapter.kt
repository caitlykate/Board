package com.caitlykate.bulletinboard.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.frag.SelectImageItem

class EditAdsViewPagerImgAdapter: RecyclerView.Adapter<EditAdsViewPagerImgAdapter.ImageHolder>() {
    val mainArray = ArrayList<SelectImageItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditAdsViewPagerImgAdapter.ImageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_pager_item, parent, false)
        return ImageHolder(view)
    }

    override fun onBindViewHolder(holder: EditAdsViewPagerImgAdapter.ImageHolder, position: Int) {   //достаем эл-ты из массива и заполняем VH
        holder.setData(mainArray[position].imageURI)
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    class ImageHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        lateinit var imgItem : ImageView
        fun setData(uri: String){
            imgItem = itemView.findViewById(R.id.imgVPItem)
            imgItem.setImageURI(Uri.parse(uri))

        }
    }

    fun update(newArray: ArrayList<SelectImageItem>){
        mainArray.clear()
        mainArray.addAll(newArray)
        notifyDataSetChanged()
    }
}