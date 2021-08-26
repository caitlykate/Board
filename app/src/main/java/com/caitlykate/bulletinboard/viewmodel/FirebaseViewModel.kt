package com.caitlykate.bulletinboard.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.caitlykate.bulletinboard.model.Ad
import com.caitlykate.bulletinboard.model.DBManager

//не разрушается когда пересоздается активити, например при повороте экрана
class FirebaseViewModel: ViewModel() {
    private val dbManager = DBManager()
    //берет наши данные, следит когда view доступна, чтобы обновлять
    val liveAdsData = MutableLiveData<ArrayList<Ad>>()

    fun loadAllAds(){
        dbManager.getAllAds(object: DBManager.ReadDataCallback{
            //запускается когда мы получили данные на дбМенеджере
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadMyAds(){
        dbManager.getMyAds(object: DBManager.ReadDataCallback{
            //запускается когда мы получили данные на дбМенеджере
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }
}