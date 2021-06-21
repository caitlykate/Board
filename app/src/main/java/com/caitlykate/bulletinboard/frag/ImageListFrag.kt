package com.caitlykate.bulletinboard.frag

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.databinding.ListImageFragBinding
import com.caitlykate.bulletinboard.utils.ImageManager
import com.caitlykate.bulletinboard.utils.ImagePicker
import com.caitlykate.bulletinboard.utils.ItemTouchMoveCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ImageListFrag(val fragCloseInterface: FragmentCloseInterface, private val newList: ArrayList<String>?): Fragment() {
    lateinit var rootElement: ListImageFragBinding
    val adapter = SelectImageRwAdapter()
    private val dragCallback = ItemTouchMoveCallback(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    private var job: Job? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.list_image_frag, container, false)
        //super.onCreateView(inflater, container, savedInstanceState)
        rootElement = ListImageFragBinding.inflate(inflater)
        return rootElement.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        //val rcView = view.findViewById<RecyclerView>(R.id.rcViewSelectImage)
        touchHelper.attachToRecyclerView(rootElement.rcViewSelectImage)
        rootElement.rcViewSelectImage.layoutManager = LinearLayoutManager(activity)                    //указываем, что наши элемнеты в РВ будут располагаться друг под другом
        rootElement.rcViewSelectImage.adapter = adapter
        if (newList != null)                                        //выбрали картинки со смартфона и есть ссылки
        job = CoroutineScope(Dispatchers.Main).launch {             //сама корутина выполняется на основном потоке
            val bitmapList = ImageManager.imageResize(newList)      //одна из задач корутины выполняется на второстепенном
            //дальше не запустится, пока suspend fun выше не выполнится
            Log.d("MyLog", "Result: $bitmapList")
            adapter.updateAdapter(bitmapList, true)
        } else{                                                     //уже готовые битмапы из EditAdsAct

        }


    }

    override fun onDetach() {
        super.onDetach()
        fragCloseInterface.onFragClose(adapter.mainArray)
        /*Log.d("MyLog", "Title 0: ${adapter.mainArray[0].title}")
        Log.d("MyLog", "Title 1: ${adapter.mainArray[1].title}")
        Log.d("MyLog", "Title 2: ${adapter.mainArray[2].title}")*/
        job?.cancel()
    }

    //настраиваем тулбар: подключаем меню, слушатель нажатий
    private fun setUpToolbar(){
        rootElement.tb.inflateMenu(R.menu.main_choose_image)
        val deleteImageItem = rootElement.tb.menu.findItem(R.id.id_delete_image)
        val addImageItem = rootElement.tb.menu.findItem(R.id.id_add_image)

        //слушатель нажатий для кнопки назад на тулбаре
        rootElement.tb.setNavigationOnClickListener{
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()     //закрываем фрагмент
        }

        deleteImageItem.setOnMenuItemClickListener {
            adapter.updateAdapter(ArrayList(), true)
            true
        }
        addImageItem.setOnMenuItemClickListener {
            val imageCount = ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size
            ImagePicker.getImages(activity as AppCompatActivity, imageCount)
            true
        }
    }

    fun updateAdapter(newList: ArrayList<String>){
        job = CoroutineScope(Dispatchers.Main).launch {
            val bitmapList = ImageManager.imageResize(newList)
            Log.d("MyLog", "Result: $bitmapList")
            adapter.updateAdapter(bitmapList, false)
        }

    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>){
        adapter.updateAdapter(bitmapList, true)
    }

}