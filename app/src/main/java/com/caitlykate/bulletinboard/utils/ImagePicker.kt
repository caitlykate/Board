package com.caitlykate.bulletinboard.utils

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.act.EditAdsAct
import com.caitlykate.bulletinboard.frag.ImageListFrag
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImagePicker {                //получаем картинки, чтобы потом показать их в списке и тд

    const val REQUEST_CODE_GET_IMAGES = 999
    const val MAX_IMAGE_COUNT = 3

    fun getOptions(imageCount: Int): Options {
        val options = Options().apply {
            count = imageCount
            isFrontFacing = false
            mode = Mode.Picture
            path = "/pix/images"
        }
        return options
    }

    fun chooseImages(edAct: EditAdsAct, imageCount: Int) {
        Log.d("MyLog2", "chooseImageFrag = $edAct.chooseImageFrag")
        edAct.addPixToActivity(R.id.placeHolder, getOptions(imageCount)) {
            //весь контент экрана (scrollViewMain и тд) заменится на фрагмент библиотеки пикс
                result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {//use results as it.data
                    Log.d("MyLog2", "Case 3")
                    getSelectedImages(edAct, result.data)
                    closePixFrag(edAct)

                //PixEventCallback.Status.BACK_PRESSED -> // back pressed called
                }
            }
        }
    }

    fun addImages(edAct: EditAdsAct, imageCount: Int) {

        Log.d("MyLog2", "chooseImageFrag = $edAct.chooseImageFrag")
        edAct.addPixToActivity(R.id.placeHolder, getOptions(imageCount)) {
            //весь контент экрана (scrollViewMain и тд) заменится на фрагмент библиотеки пикс
                result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {//use results as it.data

                        Log.d("MyLog2", "Case 4")
                        //пользователь уже выбирал картинки раньше
                        // и наш фрагмент уже был открыт, значит просто обновляем адаптер

                        replaceToChooseImageFrag(edAct)
                        edAct.chooseImageFrag?.updateAdapter(result.data as ArrayList<Uri>, edAct)

                }
            }
        }
    }

    private fun closePixFrag(edAct: EditAdsAct) {
        Log.d("MyLog", "Done")
        val fList = edAct.supportFragmentManager.fragments
        fList.forEach { frag ->
            if (frag.isVisible) edAct.supportFragmentManager.beginTransaction().remove(frag)
                .commit()
        }


    }

    private fun getSelectedImages(edAct: EditAdsAct, uris: List<Uri>){
    Log.d("MyLog2", "uris.size = ${uris.size}, chooseImageFrag = $edAct.chooseImageFrag")
        if (uris.size > 1 && edAct.chooseImageFrag == null){
            Log.d("MyLog2", "Case 1")
            //первый раз выбираем фото + картинок несколько,
            //значит нужно открыть фрагмент для премешивания,
            // он заменит фрагмент пикс
            edAct.openChooseImageFrag(uris as ArrayList<Uri>)
        } else if(uris.size == 1 && edAct.chooseImageFrag == null){
            Log.d("MyLog2", "Case 2")
            //первый раз выбираем фото, но картинка одна, перемешивать нечего,
            //фрагмента не было и не нужен
            CoroutineScope(Dispatchers.Main).launch {
                edAct.rootElement.pBarLoad.visibility = View.VISIBLE
                val bitMapArray = ImageManager.imageResize(uris as ArrayList<Uri>,edAct) as ArrayList<Bitmap>
                edAct.rootElement.pBarLoad.visibility = View.GONE
                edAct.imageAdapter.update(bitMapArray)
                closePixFrag(edAct) //нужно закрыть т.к мы не открываем новый фрагмент с редактированием
            }
        }

    }

    //передаем старый фрагмент
    private fun replaceToChooseImageFrag(edAct: EditAdsAct){
        edAct.supportFragmentManager.beginTransaction().replace(R.id.placeHolder, edAct.chooseImageFrag!!).commit()
    }
/*
    fun showSelectedImage(resultCode: Int, requestCode: Int, data: Intent?, edAct: EditAdsAct){
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == REQUEST_CODE_GET_IMAGES) {
            if (data != null) {
                val returnValues = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                //первый раз выбираем фото + картинок несколько,
                //значит нужно открыть фрагмент для премешивания
                if (returnValues?.size!! > 1 && edAct.chooseImageFrag == null)  {
                    edAct.openChooseImageFrag(returnValues)
                }
                //первый раз выбираем фото, но картинка одна, перемешивать нечего,
                //фрагмента не было и не нужен
                else if (returnValues.size == 1 && edAct.chooseImageFrag == null) {
                    //корутина т.к нужно время, чтобы превратить в битмап
                    CoroutineScope(Dispatchers.Main).launch {
                        edAct.rootElement.pBarLoad.visibility = View.VISIBLE
                        val bitMapArray = ImageManager.imageResize(returnValues) as ArrayList<Bitmap>
                        edAct.rootElement.pBarLoad.visibility = View.GONE
                        edAct.imageAdapter.update(bitMapArray)

                    }
                }
                //пользователь уже выбирал картинки раньше
                // и наш фрагмент уже был открыт, значит просто обновляем адаптер
                else if (edAct.chooseImageFrag != null) {
                    edAct.chooseImageFrag?.updateAdapter(returnValues)
                }
            }
        }
    }*/
/*
    private fun singleImage(edAct: EditAdsAct, uri: Uri){
        edAct.chooseImageFrag?.setSingleImage(uri, edAct.editImagePos)
    }*/
}