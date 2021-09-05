package com.caitlykate.bulletinboard.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.caitlykate.bulletinboard.model.Ad
import com.caitlykate.bulletinboard.model.DBManager

//не разрушается когда пересоздается активити, например при повороте экрана
class FirebaseViewModel : ViewModel() {
    private val dbManager = DBManager()

    //берет наши данные, следит когда view доступна, чтобы обновлять
    val liveAdsData = MutableLiveData<ArrayList<Ad>>()

    fun loadAllAds() {
        dbManager.getAllAds(object : DBManager.ReadDataCallback {
            //запускается когда мы получили данные на дбМенеджере
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadMyAds() {
        dbManager.getMyAds(object : DBManager.ReadDataCallback {
            //запускается когда мы получили данные на дбМенеджере
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadMyFavs() {
        dbManager.getMyFavs(object : DBManager.ReadDataCallback {
            //запускается когда мы получили данные на дбМенеджере
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun deleteItem(ad: Ad) {
        dbManager.deleteAd(ad, object : DBManager.FinishWorkListener {
            override fun onFinish() {
                val updatedList = liveAdsData.value
                updatedList?.remove(ad)
                liveAdsData.postValue(updatedList)
            }

        })
    }

    fun adViewed(ad: Ad) {
        dbManager.adViewed(ad)
    }

    fun onFavClick(ad: Ad){
        dbManager.onFavClick(ad, object : DBManager.FinishWorkListener{
            override fun onFinish() {
                val updatedList = liveAdsData.value
                val pos = updatedList?.indexOf(ad)
                if (pos != -1) { //не найдено
                    //liveAdsData не обновляет адаптер если мы у одного элемента
                    //меняем значение переменной, нужно изменить весь элемент
                        val favCounter = if (ad.isFav) ad.favCounter.toInt() - 1 else ad.favCounter.toInt() + 1
                    pos?.let {updatedList[pos] = updatedList[pos].copy(isFav = !ad.isFav, favCounter = favCounter.toString())}

                }
                liveAdsData.postValue(updatedList)
            }

        })
    }
}