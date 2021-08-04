package com.caitlykate.bulletinboard.act

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.adapters.EditAdsViewPagerImgAdapter
import com.caitlykate.bulletinboard.data.Ad
import com.caitlykate.bulletinboard.database.DBManager
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
    private val dbManager = DBManager(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityEditAdsBinding.inflate(layoutInflater)        //инициализируем
        setContentView(rootElement.root)                                    //запускаем
        init()
        dbManager.readDataFromDb()
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
        if (rootElement.tvCities.text != getString(R.string.select_city)) rootElement.tvCities.setText(
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
            dialog.showSpinnerDialog(this, listCities, rootElement.tvCities)
        }

    }

    fun onClickSelectCat(view: View){
       val listCat = resources.getStringArray(R.array.category).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, listCat, rootElement.tvCat)

    }

    fun onClickPublish(view: View){

        dbManager.publishAd(fillAd())


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
                    tvCities.text.toString(),
                    edTel.text.toString(),
                    checkBox.isChecked.toString(),
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