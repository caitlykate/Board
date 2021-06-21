package com.caitlykate.bulletinboard.frag

import android.graphics.Bitmap

interface FragmentCloseInterface {              //смотрим, если фрагмент закрылся, возвращаем скрытые элементы на активити
    fun onFragClose(list: ArrayList<Bitmap>){

    }
}