package com.caitlykate.bulletinboard.utils

import androidx.appcompat.app.AppCompatActivity
import com.fxn.pix.Options
import com.fxn.pix.Pix

object ImagePicker {                //получаем картинки, чтобы потом показать их в списке и тд
    const val REQUEST_CODE_GET_IMAGES = 999
    const val MAX_IMAGE_COUNT = 3
    fun getImages(context: AppCompatActivity, imageCount: Int){
        val options = Options.init()
                .setRequestCode(REQUEST_CODE_GET_IMAGES) //Request code for activity results
                .setCount(imageCount) //Number of images to restict selection count
                .setFrontfacing(false) //Front Facing camera on start
                .setMode(Options.Mode.Picture) //Option to select only pictures or videos or both
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT) //Orientaion
                .setPath("/pix/images") //Custom Path For media Storage

        Pix.start(context, options)
    }
}