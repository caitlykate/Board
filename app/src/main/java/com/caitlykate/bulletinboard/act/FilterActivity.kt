package com.caitlykate.bulletinboard.act

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.databinding.ActivityFilterBinding
import com.caitlykate.bulletinboard.dialogs.DialogSpinnerHelper
import com.caitlykate.bulletinboard.utils.CityHelper
import java.lang.StringBuilder

class FilterActivity : AppCompatActivity() {

    lateinit var binding: ActivityFilterBinding
    private val dialog = DialogSpinnerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        actionBarSettings()
        onClickSelectCountry()
        onClickSelectCity()
        onClickApply()
        getFilter()
    }

    private fun actionBarSettings(){
        val actBar = supportActionBar
        actBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    private fun getFilter() = with(binding){
        val filter = intent.getStringExtra(FILTER_KEY)
        if (filter != "empty" && filter != null){
            val filterArray = filter.split("_")
            if (filterArray[0] != getString(R.string.select_country)) tvCountry.text = filterArray[0]
            if (filterArray[1] != getString(R.string.select_city)) tvCity.text = filterArray[1]
            checkBoxWithSend.isChecked = filterArray[2].toBoolean()
        }
    }

    ////////////////////onClicks

    private fun onClickSelectCountry() = with(binding){
        tvCountry.setOnClickListener {
            val listCountry = CityHelper.getAllCountries(this@FilterActivity)
            dialog.showSpinnerDialog(this@FilterActivity, listCountry, tvCountry)
            if (tvCity.text != getString(R.string.select_city)) tvCity.setText(
                getString(
                    R.string.select_city
                )
            ) }
    }

    private fun onClickSelectCity() = with(binding) {
        tvCity.setOnClickListener {
            val selectedCountry = tvCountry.text.toString()
            if (selectedCountry == getString(R.string.select_country)) {
                Toast.makeText(this@FilterActivity, "Необходимо сначала выбрать страну", Toast.LENGTH_SHORT).show()
            } else {
                val listCities = CityHelper.getAllCities(selectedCountry, this@FilterActivity)
                dialog.showSpinnerDialog(this@FilterActivity, listCities, tvCity)
            }
        }
    }

    private fun onClickApply() = with(binding) {
        btApply.setOnClickListener {
            Toast.makeText(this@FilterActivity, "apply", Toast.LENGTH_SHORT).show()
            //создаем строку с фильтром
            val filter = createFilter()
            Log.d("MyLog", filter)
            val i = Intent().putExtra(FILTER_KEY, filter)
            setResult(RESULT_OK, i)
            finish()
        }
    }

    private fun createFilter(): String = with(binding) {
        val sBuilder = StringBuilder()
        val arrayTempFilter = listOf(tvCountry.text, tvCity.text, checkBoxWithSend.isChecked.toString())
        for ((i,s) in arrayTempFilter.withIndex()){
            if (s != getString(R.string.select_country) && s != getString(R.string.select_city) && s.isNotEmpty()){
                sBuilder.append(s)
                if (i != arrayTempFilter.size-1) sBuilder.append('_')
            }
        }
        return sBuilder.toString()
    }

    companion object {
        const val FILTER_KEY = "filter_key"
    }
}