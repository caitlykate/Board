package com.caitlykate.bulletinboard.act

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.adapters.ImageAdapter
import com.caitlykate.bulletinboard.databinding.ActivityDescriptionBinding
import com.caitlykate.bulletinboard.model.Ad
import com.caitlykate.bulletinboard.utils.ImageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DescriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityDescriptionBinding
    lateinit var imageAdapter: ImageAdapter
    private lateinit var ad: Ad

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        binding.fbTel.setOnClickListener{
            call()
        }
        binding.fbEmail.setOnClickListener{
            sendEmail()
        }
    }

    private fun init(){
        imageAdapter = ImageAdapter()
        binding.apply {
            viewPager.adapter = imageAdapter
        }
        getIntentFromMainAct()
        imageChangeCounter()
    }

    private fun getIntentFromMainAct(){
        ad = intent.getSerializableExtra("AD") as Ad
        updateUI(ad)
    }

    private fun updateUI(ad: Ad){
        ImageManager.fillImageArray(ad,imageAdapter)
        fillTextViews(ad)
    }

    private fun fillTextViews(ad: Ad) = with(binding) {
        tvTitle.text = ad.title
        tvPrice.text = getString(R.string.price_rub, ad.price)
        tvCat.text = ad.category
        tvCountry.text = ad.country
        tvCity.text = ad.city
        tvWithSent.text = isWithSent(ad.withSend.toBoolean())
        tvDescription.text = ad.description

    }

    private fun isWithSent(withSent: Boolean): String {
        return if (withSent) getString(R.string.yes) else getString(R.string.no)
    }

    private fun call(){
        val callUri = "tel: ${ad.tel}"
        val iCall = Intent(Intent.ACTION_DIAL)
        iCall.data = callUri.toUri()
        startActivity(iCall)
    }

    private fun sendEmail(){
        val iSendEmail = Intent(Intent.ACTION_SEND)
        iSendEmail.type = "message/rfc822"
        iSendEmail.apply{
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ad.email))
            putExtra(Intent.EXTRA_SUBJECT, arrayOf("Объявление"))
            putExtra(Intent.EXTRA_TEXT, arrayOf("Здравствуйте! Меня интересует ваше объявление '${ad.title}'"))
        }
        try {
            startActivity(Intent.createChooser(iSendEmail, "Открыть с помощью"))
        } catch (e: ActivityNotFoundException){
            Toast.makeText(this, "Нет подходящего приложения", Toast.LENGTH_LONG).show()
        }
    }

    private fun imageChangeCounter(){
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imageCounter = "${position+1} / ${binding.viewPager.adapter?.itemCount}"
                binding.tvImageCounter.text = imageCounter
            }
        })
    }

}