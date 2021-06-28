package com.caitlykate.bulletinboard.frag

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.caitlykate.bulletinboard.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.*


//здесь прописываем рекламу, чтобы не захламлять основной код в дочерних фрагментах (!там должен быть adView!)
open class BaseAdsFrag: Fragment(), InterAdsClose {
    lateinit var adView: AdView
    var interAd: InterstitialAd? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAds()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadInterAd()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onPause() {
        super.onPause()
        adView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        adView.destroy()
    }

    fun initAds(){
        MobileAds.initialize(activity as Activity)
        var adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    //подготовка рекламы
    fun loadInterAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context as Activity, getString(R.string.ad_inter_id), adRequest,
                            object: InterstitialAdLoadCallback(){
                                override fun onAdLoaded(ad: InterstitialAd) {
                                    interAd = ad
                                }
                            })
    }

    //показываем рекламу
    fun showInterAd(){
        if (interAd != null){

            //callback кот. следит за тем, что происходит с рекламой (закрыли/ошибка/показаласьюю)
            interAd?.fullScreenContentCallback = object: FullScreenContentCallback(){
                override fun onAdDismissedFullScreenContent() {             //пользователь закрыл рекламу
                    //в базовом фрагменте не указываем конкретных действий при закрытии рекламы, работаем через интерфейс
                    onClose()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {   //если ошибка, то все равно закрыть
                    onClose()
                }
            }
            interAd?.show(activity)
        } else onClose()
    }

    override fun onClose() {
        //эту функцию реализуем в дочерних фрагментах
    }
}