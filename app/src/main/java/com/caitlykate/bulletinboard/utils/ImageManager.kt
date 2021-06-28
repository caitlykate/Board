package com.caitlykate.bulletinboard.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

object ImageManager {
    private const val MAX_IMAGE_SIZE = 1000
    private const val WIDTH = 0
    private const val HEIGHT = 1

    fun getImageSize(uri: String): List<Int>{

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true        //берем только края картинки, чтобы если она слишком большая не словить ошибку
        }
        BitmapFactory.decodeFile(uri, options)

        return if (imgRotation(uri) == 0) listOf(options.outWidth, options.outHeight) else listOf(options.outHeight, options.outWidth)
    }


    private fun imgRotation(uri: String): Int{
        val rotation : Int
        val imgFile = File(uri)
        val exif = ExifInterface(imgFile.absoluteFile)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        rotation = if (orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                       orientation == ExifInterface.ORIENTATION_ROTATE_270) 90 else 0
        return rotation
    }

    //Корутины основаны на ключевом слове suspend, которое используется для того,
    // чтобы показать, что функция может быть прервана.
    // Другими словами, вызов такой функции может быть прерван в любой момент.
    // Такие функции могут быть вызваны только из корутин, которым, в свою очередь,
    // требуется по крайней мере одна запущенная функция.
    suspend fun imageResize(uris: List<String>): List<Bitmap> = withContext(Dispatchers.IO) { //ArrayList<List<Int>>{
        val tempList = ArrayList<List<Int>>()
        val bitmapList = ArrayList<Bitmap>()
        //val stringList = ArrayList<String>()
        for (n in uris.indices){
            val size = getImageSize(uris[n])
            Log.d("MyLog", "Width: ${size[WIDTH]} Height: ${size[HEIGHT]}")
            val imageRatio = size[WIDTH].toFloat()/size[HEIGHT].toFloat()        //соотношение м/у сторонами
            if (imageRatio > 1){
                if (size[WIDTH] > MAX_IMAGE_SIZE) {
                    tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE/imageRatio).toInt()))
                } else tempList.add(listOf(size[WIDTH], size[HEIGHT]))
            } else {
                if (size[HEIGHT] > MAX_IMAGE_SIZE) {
                    tempList.add(listOf((MAX_IMAGE_SIZE*imageRatio).toInt(), MAX_IMAGE_SIZE))
                } else tempList.add(listOf(size[WIDTH], size[HEIGHT]))
            }
            Log.d("MyLog", "Ratio: $imageRatio")
            Log.d("MyLog", "New sizes: ${tempList[n]}")
        }


        for (n in uris.indices) {
            val e = kotlin.runCatching {
                //превращаем каждую uri в битмап нужного размера
                bitmapList.add(
                    Picasso.get().load(File(uris[n]))
                        .resize(tempList[n][WIDTH], tempList[n][HEIGHT]).get()
                )
            }
            Log.d("MyLog", "Bitmap load done: ${e.isSuccess}")
        }

        return@withContext bitmapList

    }

    fun chooseScaleType(im: ImageView, bitmap: Bitmap){
        if (bitmap.width>bitmap.height){
            //обрезаем
            im.scaleType = ImageView.ScaleType.CENTER_CROP
        } else im.scaleType = ImageView.ScaleType.CENTER_INSIDE
    }

}