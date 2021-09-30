package com.caitlykate.bulletinboard.utils


import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import com.caitlykate.bulletinboard.adapters.ImageAdapter
import com.caitlykate.bulletinboard.model.Ad
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

object ImageManager {
    private const val MAX_IMAGE_SIZE = 1000
    private const val WIDTH = 0
    private const val HEIGHT = 1

/*
    private fun File.copyInStreamToFile(inStream: InputStream) {   //расширяем стандартный класс File
        //inStream получаем contentResolver
        //this - File
        this.outputStream().use{
            out -> inStream.copyTo(out)             //во временный файл, к кот, мы применим функцию, скопируется поток байтов,
                                                    //чтобы потом узнать его размер, поворот и тд
        }
    }
*/

    fun getImageSize(uri: Uri, act: Activity): List<Int> {        //ссылку нам выдвет Pix
        //через посредника contentResolver можем получать доступ к файлу с помощью InputStream
        val inStream = act.contentResolver.openInputStream(uri)

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds =
                true        //берем только края картинки, чтобы если она слишком большая не словить ошибку
        }
        BitmapFactory.decodeStream(inStream, null, options)          //(uri, options)

        return listOf(options.outWidth, options.outHeight)
    }


    suspend fun imageResize(uris: ArrayList<Uri>, act: Activity): List<Bitmap> =
        withContext(Dispatchers.IO) { //ArrayList<List<Int>>{
            val tempList = ArrayList<List<Int>>()
            val bitmapList = ArrayList<Bitmap>()
            //val stringList = ArrayList<String>()
            for (n in uris.indices) {
                val size = getImageSize(uris[n], act)
                Log.d("MyLog", "Width: ${size[WIDTH]} Height: ${size[HEIGHT]}")
                val imageRatio =
                    size[WIDTH].toFloat() / size[HEIGHT].toFloat()        //соотношение м/у сторонами
                if (imageRatio > 1) {
                    if (size[WIDTH] > MAX_IMAGE_SIZE) {
                        tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt()))
                    } else tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                } else {
                    if (size[HEIGHT] > MAX_IMAGE_SIZE) {
                        tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE))
                    } else tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                }
                Log.d("MyLog", "Ratio: $imageRatio")
                Log.d("MyLog", "New sizes: ${tempList[n]}")
            }

            for (n in uris.indices) {
                val e = kotlin.runCatching {
                    //превращаем каждую uri в битмап нужного размера
                    bitmapList.add(
                        Picasso.get().load(uris[n])
                            .resize(tempList[n][WIDTH], tempList[n][HEIGHT]).get()
                    )
                }
                Log.d("MyLog", "Bitmap load done: ${e.isSuccess}")
            }
            return@withContext bitmapList
        }


    private suspend fun getBitmapFromUri(uris: List<String?>): List<Bitmap> =
        withContext(Dispatchers.IO) { //ArrayList<List<Int>>{

            val bitmapList = ArrayList<Bitmap>()
            for (n in uris.indices) {
                kotlin.runCatching {
                    bitmapList.add(Picasso.get().load(uris[n]).get())
                }
            }
            return@withContext bitmapList
        }

    fun chooseScaleType(im: ImageView, bitmap: Bitmap) {
        if (bitmap.width > bitmap.height) {
            //обрезаем
            im.scaleType = ImageView.ScaleType.CENTER_CROP
        } else im.scaleType = ImageView.ScaleType.CENTER_INSIDE
    }


    fun fillImageArray(ad: Ad, imageAdapter: ImageAdapter) {
        val listUris = listOf(ad.mainImage, ad.image2, ad.image3)
        CoroutineScope(Dispatchers.Main).launch {
            val bitmapList = ImageManager.getBitmapFromUri(listUris)
            imageAdapter.update(bitmapList as ArrayList<Bitmap>)
        }
    }
}
