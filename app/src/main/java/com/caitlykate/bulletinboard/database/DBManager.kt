package com.caitlykate.bulletinboard.database

import android.util.Log
import com.caitlykate.bulletinboard.data.Ad
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DBManager(val readDataCallback: ReadDataCallback?) {
    //объект класса вместе с ссылкой на нашу бд, кот. знает куда и что нужно записывать
    val db = Firebase.database.getReference("main")     //.reference - в корень бд
    val auth = Firebase.auth

    //у него есть спец функции для записи
    fun publishAd(ad: Ad){
        if (auth.uid != null) db.child(ad.key?: "empty").child(auth.uid!!).child("ad").setValue(ad)
    }

    fun readDataFromDb(){       //считывает что есть на этом пути (main)
        //listener запускается один раз и не обновляет в реальном времени
        db.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adArray = ArrayList<Ad>()
                for (item in snapshot.children){
                    //указываем, что хотим получить данные на указанном пути в виде нашего класса Ad
                    val ad = item.child(auth.uid!!).child("ad").getValue(Ad::class.java)
                    //Log.d("MyLog", "Data: ${ad?.tel}")
                    if(ad!=null) adArray.add(ad)
                }
                readDataCallback?.readData(adArray)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}