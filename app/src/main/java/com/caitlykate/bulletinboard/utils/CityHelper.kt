package com.caitlykate.bulletinboard.utils

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.security.AccessControlContext
import java.util.*
import kotlin.collections.ArrayList

object CityHelper {
    fun getAllCountries(context: Context):ArrayList<String>{
        var tempArray = ArrayList<String>()
        try{
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size: Int = inputStream.available()
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            val jsonFile = String(bytesArray)
            val jsonObject = JSONObject(jsonFile)
            val countryNames = jsonObject.names()
            for (i in 0 until countryNames.length()){
                tempArray.add(countryNames.getString(i))
            }
        } catch(e:IOException){

        }
        return tempArray
    }

    fun getAllCities(countryName: String, context: Context):ArrayList<String>{
        var tempArray = ArrayList<String>()
        try{
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size: Int = inputStream.available()
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            val jsonFile = String(bytesArray)
            val jsonObject = JSONObject(jsonFile)
            val citiesNames = jsonObject.getJSONArray(countryName)
            for (i in 0 until citiesNames.length()){
                tempArray.add(citiesNames.getString(i))
            }
        } catch(e:IOException){

        }
        return tempArray
    }

    fun filterListData(list: ArrayList<String>, searchText: String?): ArrayList<String>{
        val filteredList = ArrayList<String>()
        filteredList.clear()
        if (searchText == null){
            filteredList.add("No result")
            return filteredList
        }
        for (selection:String in list){                                     //сверяем каждый элемент
            if (selection.toLowerCase(Locale.ROOT).startsWith(searchText.toLowerCase(Locale.ROOT))){
                filteredList.add(selection)
            }
        }
        if (filteredList.size == 0) filteredList.add("No result")
        return filteredList
    }
}