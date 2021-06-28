package com.caitlykate.bulletinboard.frag

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.databinding.SelectImageFragItemBinding
import com.caitlykate.bulletinboard.utils.AdapterCallback
import com.caitlykate.bulletinboard.utils.ImageManager
import com.caitlykate.bulletinboard.utils.ItemTouchMoveCallback

class SelectImageRwAdapter(val adapterCallback: AdapterCallback): RecyclerView.Adapter<SelectImageRwAdapter.ImageHolder>(), ItemTouchMoveCallback.ItemTouchAdapterInterface {
    val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectImageRwAdapter.ImageHolder {
/*        val view = LayoutInflater.from(parent.context).inflate(R.layout.select_image_frag_item,parent,false)
        return ImageHolder(view, parent.context, this)*/
        val viewBinding = SelectImageFragItemBinding.inflate(LayoutInflater.from(parent.context),parent,false) //LayoutInflater.from(parent.context).inflate(R.layout.select_image_frag_item,parent,false)
        return ImageHolder(viewBinding, parent.context, this)
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

    class ImageHolder(private val viewBinding: SelectImageFragItemBinding, val context: Context, val adapter: SelectImageRwAdapter): RecyclerView.ViewHolder(viewBinding.root) {

        fun setData(bitmap: Bitmap){

            viewBinding.tvTitle.text = context.resources.getStringArray(R.array.title_array)[adapterPosition]
            ImageManager.chooseScaleType(viewBinding.imageContent, bitmap)
            viewBinding.imageContent.setImageBitmap(bitmap)

            viewBinding.imBtDelete.setOnClickListener {
                adapter.mainArray.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
                //adapter.notifyDataSetChanged() это нам бы помогла обновить и title, но тогда плавная анимация пропадает
                for (n in 0 until adapter.mainArray.size) adapter.notifyItemChanged(n)
                adapter.adapterCallback.onItemDelete()
            }
        }

    }

    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean){
        if (needClear) mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()                          //сообщаем адаптеру, что данные внутри изменились, чтобы он снова перезапустился
    }


}