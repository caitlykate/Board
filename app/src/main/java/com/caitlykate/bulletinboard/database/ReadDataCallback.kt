package com.caitlykate.bulletinboard.database

import com.caitlykate.bulletinboard.data.Ad

interface ReadDataCallback {
    //абстрактная функция, реализуем в MainAct
    fun readData(list: List<Ad>){

    }
}