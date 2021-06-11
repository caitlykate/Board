package com.caitlykate.bulletinboard.frag

import android.net.Uri
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.utils.ItemTouchMoveCallback

class SelectImageRwAdapter: RecyclerView.Adapter<SelectImageRwAdapter.ImageHolder>(), ItemTouchMoveCallback.ItemTouchAdapterInterface {
    val mainArray = ArrayList<SelectImageItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectImageRwAdapter.ImageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.select_image_frag_item,parent,false)
        return ImageHolder(view)
    }

    override fun onBindViewHolder(holder: SelectImageRwAdapter.ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onMove(startPos: Int, targetPos: Int) {
        //необходимо запомнить элемент, на мето которого мы ставим тот, что тащим
        /*val targetItem = mainArray[targetPos]
        mainArray[targetPos] = mainArray[startPos]
        mainArray[startPos] = targetItem*/
        val targetItemUri = mainArray[targetPos].imageURI
        mainArray[targetPos].imageURI = mainArray[startPos].imageURI
        mainArray[startPos].imageURI = targetItemUri
        notifyItemMoved(startPos,targetPos)
    }

    override fun onClear() {
        notifyDataSetChanged()
    }

    class ImageHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        lateinit var tvTitle: TextView
        lateinit var imageView: ImageView
        fun setData(item: SelectImageItem){
            tvTitle = itemView.findViewById(R.id.tvTitle)
            imageView = itemView.findViewById(R.id.imageContent)
            tvTitle.text = item.title
            imageView.setImageURI(Uri.parse(item.imageURI))
        }

    }

    fun updateAdapter(newList: List<SelectImageItem>){
        mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()                          //сообщаем адаптеру, что данные внутри изменились, чтобы он снова перезапустился
    }


}