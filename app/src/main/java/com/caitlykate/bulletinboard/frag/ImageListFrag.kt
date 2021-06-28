package com.caitlykate.bulletinboard.frag

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.databinding.ListImageFragBinding
import com.caitlykate.bulletinboard.dialoghelper.ProgressDialog
import com.caitlykate.bulletinboard.utils.AdapterCallback
import com.caitlykate.bulletinboard.utils.ImageManager
import com.caitlykate.bulletinboard.utils.ImagePicker
import com.caitlykate.bulletinboard.utils.ItemTouchMoveCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ImageListFrag(private val fragCloseInterface: FragmentCloseInterface, private val newList: ArrayList<String>?): BaseAdsFrag(), AdapterCallback {

    val adapter = SelectImageRwAdapter(this)
    private val dragCallback = ItemTouchMoveCallback(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    var addImageItem: MenuItem? = null
    private var job: Job? = null
    lateinit var binding: ListImageFragBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ListImageFragBinding.inflate(layoutInflater)
        adView = binding.adView                        //инициализируем переменную родительского класса
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        binding.apply {         //чтобы не писать везде binding.
            touchHelper.attachToRecyclerView(rcViewSelectImage)
            rcViewSelectImage.layoutManager =
                LinearLayoutManager(activity)                    //указываем, что наши элемнеты в РВ будут располагаться друг под другом
            rcViewSelectImage.adapter = adapter
        }
        if (newList != null) resizeSelectedImages(newList, true)                                       //выбрали картинки со смартфона и есть ссылки
        else{                                                     //уже готовые битмапы из EditAdsAct

        }
    }
    override fun onItemDelete() {
        addImageItem?.isVisible = true
    }

    override fun onDetach() {
        super.onDetach()
        fragCloseInterface.onFragClose(adapter.mainArray)
        job?.cancel()
    }

    private fun resizeSelectedImages(newList: ArrayList<String>, needClear: Boolean){
        job = CoroutineScope(Dispatchers.Main).launch {             //сама корутина выполняется на основном потоке
            val dialog = ProgressDialog.createProgressDialog(activity as Activity)
            val bitmapList = ImageManager.imageResize(newList)      //одна из задач корутины выполняется на второстепенном
            //дальше не запустится, пока suspend fun выше не выполнится
            Log.d("MyLog", "Result: $bitmapList")
            dialog.dismiss()
            adapter.updateAdapter(bitmapList, needClear)
            if (adapter.mainArray.size > 2) addImageItem?.isVisible = false
        }
    }

    //настраиваем тулбар: подключаем меню, слушатель нажатий
    private fun setUpToolbar(){
        binding.apply {
            tb.inflateMenu(R.menu.main_choose_image)
            val deleteImageItem = tb.menu.findItem(R.id.id_delete_image)
            addImageItem = tb.menu.findItem(R.id.id_add_image)

            //слушатель нажатий для кнопки назад на тулбаре
            tb.setNavigationOnClickListener {
                showInterAd()   //прописана в базовом фрагменте
            }

            deleteImageItem.setOnMenuItemClickListener {
                adapter.updateAdapter(ArrayList(), true)
                addImageItem?.isVisible = true
                true
            }
            addImageItem?.setOnMenuItemClickListener {
                val imageCount = ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size
                ImagePicker.getImages(activity as AppCompatActivity, imageCount)
                true
            }
        }
    }

    fun updateAdapter(newList: ArrayList<String>){
        resizeSelectedImages(newList, false)

    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>){
        adapter.updateAdapter(bitmapList, true)
    }

    override fun onClose() {
        super.onClose()
        //удаляем фрагмент, остается активити
        activity?.supportFragmentManager?.beginTransaction()?.remove(this@ImageListFrag)?.commit()
    }



}