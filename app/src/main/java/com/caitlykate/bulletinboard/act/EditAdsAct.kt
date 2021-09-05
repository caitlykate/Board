package com.caitlykate.bulletinboard.act

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.caitlykate.bulletinboard.MainActivity
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.adapters.EditAdsViewPagerImgAdapter
import com.caitlykate.bulletinboard.model.Ad
import com.caitlykate.bulletinboard.model.DBManager
import com.caitlykate.bulletinboard.databinding.ActivityEditAdsBinding
import com.caitlykate.bulletinboard.dialogs.DialogSpinnerHelper
import com.caitlykate.bulletinboard.frag.FragmentCloseInterface
import com.caitlykate.bulletinboard.frag.ImageListFrag
import com.caitlykate.bulletinboard.utils.CityHelper
import com.caitlykate.bulletinboard.utils.ImageManager
import com.caitlykate.bulletinboard.utils.ImagePicker
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class EditAdsAct: AppCompatActivity(), FragmentCloseInterface {
    private var chooseImageFrag: ImageListFrag? = null
    lateinit var rootElement: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    private lateinit var imageAdapter: EditAdsViewPagerImgAdapter
    private val dbManager = DBManager()
    private var isEditState = false
    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityEditAdsBinding.inflate(layoutInflater)        //инициализируем
        setContentView(rootElement.root)                                    //запускаем
        init()
        //dbManager.readDataFromDb()
        checkEditState()
    }
        /*
        //создаем адаптер, подключаем к спиннеру
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, CityHelper.getAllCountries(this))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)                                          //???
        rootElement.spCountry.adapter = adapter*/
    private fun init(){
        imageAdapter = EditAdsViewPagerImgAdapter()
        rootElement.vpImages.adapter = imageAdapter
    }

    private fun checkEditState(){
        if (isEditState()){
            isEditState = true
            ad = intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad
            if (ad != null) fillViews(ad!!)
        }

    }

    private fun isEditState(): Boolean{
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(ad: Ad) = with(rootElement){
        tvCountry.text = ad.country
        tvCity.text = ad.city
        edTel.setText(ad.tel)
        checkBoxWithSend.isChecked = ad.withSend.toBoolean()
        tvCat.text = ad.category
        edTitle.setText(ad.title)
        edPrice.setText(ad.price)
        edDescription.setText(ad.description)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == ImagePicker.REQUEST_CODE_GET_IMAGES) {
            if (data != null) {
                val returnValues = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)

                if (returnValues?.size!! > 1 && chooseImageFrag == null)  openChooseImageFrag(returnValues)
                else if (returnValues.size == 1 && chooseImageFrag == null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        rootElement.pBarLoad.visibility = View.VISIBLE
                        val bitMapArray = ImageManager.imageResize(returnValues) as ArrayList<Bitmap>
                        rootElement.pBarLoad.visibility = View.GONE
                        imageAdapter.update(bitMapArray)

                    }
                }
                else if (chooseImageFrag != null) chooseImageFrag?.updateAdapter(returnValues)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    ImagePicker.getImages(this, 3)
                } else {

                    Toast.makeText(
                        this,
                        "Approve permissions to open Pix ImagePicker",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
    }

    ////////////////////onClicks

    fun onClickSelectCountry(view: View){
        val listCountry = CityHelper.getAllCountries(this)
        dialog.showSpinnerDialog(this, listCountry, rootElement.tvCountry)
        if (rootElement.tvCity.text != getString(R.string.select_city)) rootElement.tvCity.setText(
            getString(
                R.string.select_city
            )
        )
    }

    fun onClickSelectCity(view: View){
        val selectedCountry = rootElement.tvCountry.text.toString()
        if (selectedCountry == getString(R.string.select_country)){
            Toast.makeText(this, "Необходимо сначала выбрать страну", Toast.LENGTH_SHORT).show()
        } else {
            val listCities = CityHelper.getAllCities(selectedCountry, this)
            dialog.showSpinnerDialog(this, listCities, rootElement.tvCity)
        }

    }

    fun onClickSelectCat(view: View){
       val listCat = resources.getStringArray(R.array.category).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, listCat, rootElement.tvCat)

    }

    fun onClickPublish(view: View){
        var tempAd = fillAd()
        if (isEditState){
            dbManager.publishAd(tempAd.copy(key = ad?.key), onPublishFinish())
        } else {
            dbManager.publishAd(tempAd, onPublishFinish())
        }
    }

    //ф-я возвращает интерфейс, кот. мы передаем в функцию publishAd, как только данные опубликовались в базе,
    // запускается addOnCompleteListener, кот. вызывает ф-ю onFinish, инструкция к которой описана здесь
    private fun onPublishFinish(): DBManager.FinishWorkListener{
        return object: DBManager.FinishWorkListener{
            override fun onFinish() {
                finish()
            }

        }
    }

    fun onClickGetImagesOrOpenFrag(view: View) {
        if (imageAdapter.mainArray.size == 0)
            ImagePicker.getImages(this, 3)
        else {
            //битмапы есть - переносим во фрагмент
            openChooseImageFrag(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }


    override fun onFragClose(list: ArrayList<Bitmap>) {                                    //запускается, когда возвращаемся из фрагмента редактирования
        rootElement.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null
    }
    
    private fun openChooseImageFrag(imgList: ArrayList<String>?){
        Log.d("MyLog", "1")
        chooseImageFrag = ImageListFrag(this, imgList)
        rootElement.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.placeHolder, chooseImageFrag!!)
        fm.commit()
        Log.d("MyLog", "2")
    }

    private fun fillAd(): Ad{
        val ad: Ad
        rootElement.apply {
            //нужно будет добавить проверку полей
            ad = Ad(tvCountry.text.toString(),
                    tvCity.text.toString(),
                    edTel.text.toString(),
                    checkBoxWithSend.isChecked.toString(),
                    edTitle.text.toString(),
                    tvCat.text.toString(),
                    edPrice.text.toString(),
                    edDescription.text.toString(),
                    dbManager.db.push().key,           //генерирует уникальный ключ
                                                      //если написать просто dbManager.db. key, то вернет path (main)
                    dbManager.auth.uid
            )
        }
        return ad
    }
}