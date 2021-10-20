package com.caitlykate.bulletinboard.utils

import com.caitlykate.bulletinboard.model.Ad
import com.caitlykate.bulletinboard.model.AdFilter

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
}