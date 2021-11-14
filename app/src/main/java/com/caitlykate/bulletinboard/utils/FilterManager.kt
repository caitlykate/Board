package com.caitlykate.bulletinboard.utils

import com.caitlykate.bulletinboard.model.Ad
import com.caitlykate.bulletinboard.model.AdFilter
import java.lang.StringBuilder

object FilterManager {

    fun createFilter(ad: Ad): AdFilter{
        return AdFilter(
            ad.time,
            "${ad.category}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.city}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.withSend}_${ad.time}",
            "${ad.country}_${ad.withSend}_${ad.time}",
            "${ad.country}_${ad.city}_${ad.withSend}_${ad.time}",
            "${ad.withSend}_${ad.time}"
        )
    }

    fun getFilterForDB(filter: String): String{
        val sBuilderNode = StringBuilder()
        val sBuilderFilter = StringBuilder()
        val tempArray = filter.split("_")
        if (tempArray[0] != "empty") {
            sBuilderNode.append("country_")
            sBuilderFilter.append("${tempArray[0]}_")
        }
        if (tempArray[1] != "empty") {
            sBuilderNode.append("city_")
            sBuilderFilter.append("${tempArray[1]}_")
        }
        sBuilderFilter.append(tempArray[2])
        sBuilderNode.append("withSent_time")
        return "$sBuilderNode|$sBuilderFilter"
    }
}