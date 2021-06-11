package com.caitlykate.bulletinboard.frag

interface FragmentCloseInterface {              //смотрим, если фрагмент закрылся, возвращаем скрытые элементы на активити
    fun onFragClose(list: ArrayList<SelectImageItem>){

    }
}