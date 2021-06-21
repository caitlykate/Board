package com.caitlykate.bulletinboard.adapters

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.caitlykate.bulletinboard.R

class EditAdsViewPagerImgAdapter: RecyclerView.Adapter<EditAdsViewPagerImgAdapter.ImageHolder>() {
    val mainArray = ArrayList<Bitmap>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditAdsViewPagerImgAdapter.ImageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_pager_item, parent, false)
        return ImageHolder(view)
    }

    override fun onBindViewHolder(holder: EditAdsViewPagerImgAdapter.ImageHolder, position: Int) {   //достаем эл-ты из массива и заполняем VH
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    class ImageHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        lateinit var imgItem : ImageView
        fun setData(bitmap: Bitmap){
            imgItem = itemView.findViewById(R.id.imgVPItem)
            //imgItem.setImageURI(Uri.parse(uri))
            imgItem.setImageBitmap(bitmap)

        }
    }

    fun update(newArray: ArrayList<Bitmap>){
        mainArray.clear()
        mainArray.addAll(newArray)
        notifyDataSetChanged()
    }
}