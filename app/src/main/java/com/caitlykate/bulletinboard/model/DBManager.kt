package com.caitlykate.bulletinboard.model

import android.util.Log
import com.caitlykate.bulletinboard.utils.FilterManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class DBManager {
    //объект класса вместе с ссылкой на нашу бд, кот. знает куда и что нужно записывать
    val db = Firebase.database.getReference(MAIN_NODE)     //.reference - в корень бд
    val auth = Firebase.auth
    val dbStorage= Firebase.storage.getReference(MAIN_NODE)

    //у него есть спец функции для записи
    fun publishAd(ad: Ad, finishListener: FinishWorkListener){
        if (auth.uid != null) db.child(ad.key?: "empty").child(auth.uid!!).child(AD_NODE).setValue(ad).addOnCompleteListener{
            val adFilter = FilterManager.createFilter(ad)
            if (auth.uid != null) db.child(ad.key?: "empty").child(FILTER_NODE).setValue(adFilter).addOnCompleteListener{
                finishListener.onFinish()
            }
        }
    }

    fun adViewed(ad: Ad){
        var counter = ad.viewsCounter.toInt()
        counter++
        if (auth.uid != null) db.child(ad.key?: "empty").child(INFO_NODE)
            .setValue(InfoItem(counter.toString(), ad.emailsCounter, ad.callsCounter))
    }

    fun onFavClick(ad: Ad, listener: FinishWorkListener){
        if (ad.isFav) removeFromFavs(ad, listener) else addToFavs(ad, listener)
    }

    private fun addToFavs(ad: Ad, listener: FinishWorkListener){
        ad.key?.let {
            auth.uid?.let {
                    uid -> db.child(it).child(FAVS_NODE).child(uid).setValue(auth.uid).addOnCompleteListener{
                        if(it.isSuccessful) listener.onFinish()
            }
            }
        }
    }

    private fun removeFromFavs(ad: Ad, listener: FinishWorkListener){
        ad.key?.let {
            auth.uid?.let {
                    uid -> db.child(it).child(FAVS_NODE).child(uid).removeValue().addOnCompleteListener{
                if(it.isSuccessful) listener.onFinish()
            }
            }
        }
    }

    fun getAllAdsByCatFirstPage(cat: String, readDataCallback: ReadDataCallback?){
        val query = db.orderByChild( "/filter/catTime")
            .startAt(cat).endAt(cat + "\uf8ff").limitToLast(ADS_LIMIT)
        readDataFromDb(query,readDataCallback)
    }

    fun getAllAdsByCatNextPage(catTime: String, readDataCallback: ReadDataCallback?){
        val query = db.orderByChild( "/filter/catTime")
            .endBefore(catTime).limitToLast(ADS_LIMIT)
        readDataFromDb(query,readDataCallback)
    }

    fun getAllAdsFirstPage(readDataCallback: ReadDataCallback?){
        val query = db.orderByChild( "/filter/time").limitToLast(ADS_LIMIT)    //берем N последних объявлений
        readDataFromDb(query,readDataCallback)
    }

    fun getAllAdsNextPage(time: String, readDataCallback: ReadDataCallback?){
        val query = db.orderByChild( "/filter/time").endBefore(time).limitToLast(ADS_LIMIT)    //берем N последних объявлений до уже загруженных в ленту
        readDataFromDb(query,readDataCallback)
    }


    fun getMyAds(readDataCallback: ReadDataCallback?){
        val query = db.orderByChild( auth.uid + "/ad/uid" ).equalTo(auth.uid)
        readDataFromDb(query,readDataCallback)
    }

    fun getMyFavs(readDataCallback: ReadDataCallback?){
        val query = db.orderByChild( "/favs/${auth.uid}" ).equalTo(auth.uid)
        readDataFromDb(query,readDataCallback)
    }

    fun deleteAd(ad: Ad, listener: FinishWorkListener){
        if (ad.key == null || ad.uid == null) return
        db.child(ad.key).child(ad.uid).removeValue().addOnCompleteListener {
            listener.onFinish()
        }

    }


    private fun readDataFromDb(query: Query, readDataCallback: ReadDataCallback?){       //считывает что есть на этом пути (main)
        //listener запускается один раз и не обновляет в реальном времени
        query.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { //все, что отфильтровали в узле main
                val adArray = ArrayList<Ad>()
                for (item in snapshot.children){        //цикл пребирает узлы с объявлениями
                    var ad: Ad? = null
                    item.children.forEach{                 //внутри узла key пробегаем по узлам uid и info, ищем объявление
                        if (ad == null) ad = it.child(AD_NODE).getValue(Ad::class.java)
                    }
                    //указываем, что хотим получить данные на указанном пути в виде нашего класса Ad
                    //val ad = item.child(auth.uid!!).child(AD_NODE).getValue(Ad::class.java)
                    //Log.d("MyLog", "Data: ${ad?.tel}")
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)
                    val favCounter = item.child(FAVS_NODE).childrenCount
                    val isFav = auth.uid?.let { item.child(FAVS_NODE).child(it).getValue(String::class.java) }
                    //перегружаем в объявление инфо о просмотрах и тд
                    ad?.isFav = isFav != null
                    ad?.favCounter = favCounter.toString()
                    ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    ad?.emailsCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    if(ad!=null) adArray.add(ad!!)      //формируем список объявлений
                }
                readDataCallback?.readData(adArray)     //отправляем на адаптер
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    interface ReadDataCallback {
        //абстрактная функция, реализуем в MainAct
        fun readData(list: ArrayList<Ad>){

        }
    }

    interface FinishWorkListener{
        fun onFinish()
    }

    companion object{
        const val AD_NODE = "ad"
        const val FILTER_NODE = "filter"
        const val MAIN_NODE = "main"
        const val INFO_NODE = "info"
        const val FAVS_NODE = "favs"
        const val ADS_LIMIT = 2
    }
}