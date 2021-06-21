package com.caitlykate.bulletinboard.frag

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.utils.ItemTouchMoveCallback

class SelectImageRwAdapter: RecyclerView.Adapter<SelectImageRwAdapter.ImageHolder>(), ItemTouchMoveCallback.ItemTouchAdapterInterface {
    val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectImageRwAdapter.ImageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.select_image_frag_item,parent,false)
        return ImageHolder(view, parent.context, this)
    }

    override fun onBindViewHolder(holder: SelectImageRwAdapter.ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onMove(startPos: Int, targetPos: Int) {
        //необходимо запомнить элемент, на место которого мы ставим тот, что тащим
        val targetItem = mainArray[targetPos]
        mainArray[targetPos] = mainArray[startPos]
        mainArray[startPos] = targetItem
        notifyItemMoved(startPos,targetPos)
    }

    override fun onClear() {
        notifyDataSetChanged()
    }

    class ImageHolder(itemView: View, val context: Context, val adapter: SelectImageRwAdapter): RecyclerView.ViewHolder(itemView) {
        lateinit var tvTitle: TextView
        lateinit var imageView: ImageView
        lateinit var imBtDel: ImageButton
        fun setData(bitmap: Bitmap){
            tvTitle = itemView.findViewById(R.id.tvTitle)
            imageView = itemView.findViewById(R.id.imageContent)
            imBtDel = itemView.findViewById(R.id.imBtDelete)
            tvTitle.text = context.resources.getStringArray(R.array.title_array)[adapterPosition]
            //imageView.setImageURI(Uri.parse(bitmap))
            imageView.setImageBitmap(bitmap)

            imBtDel.setOnClickListener {
                adapter.mainArray.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
                //adapter.notifyDataSetChanged() это нам бы помогла обновить и title, но тогда плавная анимация пропадает
                for (n in 0 until adapter.mainArray.size) adapter.notifyItemChanged(n)
            }
        }

    }

    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean){
        if (needClear) mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()                          //сообщаем адаптеру, что данные внутри изменились, чтобы он снова перезапустился
    }


}