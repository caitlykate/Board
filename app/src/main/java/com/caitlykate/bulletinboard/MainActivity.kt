
package com.caitlykate.bulletinboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.caitlykate.bulletinboard.accounthelper.AccountHelper
import com.caitlykate.bulletinboard.act.DescriptionActivity
import com.caitlykate.bulletinboard.act.EditAdsAct
import com.caitlykate.bulletinboard.act.FilterActivity
import com.caitlykate.bulletinboard.adapters.AdsRcAdapter
import com.caitlykate.bulletinboard.databinding.ActivityMainBinding
import com.caitlykate.bulletinboard.dialoghelper.DialogConst
import com.caitlykate.bulletinboard.dialoghelper.DialogHelper
import com.caitlykate.bulletinboard.model.Ad
import com.caitlykate.bulletinboard.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AdsRcAdapter.Listener {
    private lateinit var binding: ActivityMainBinding                       //вместо lateinit можно было ActivityMainBinding? = null
                                                                                //rootElement - binding
    private val dialogHelper = DialogHelper(this)
    val mAuth = Firebase.auth                                      //Или чтобы получить обьект FirebaseAuth можно вызвать
                                                                   //статический метод FirebaseAuth.getInstance()
    private lateinit var tvAccount: TextView
    private lateinit var imAccount: ImageView
    private val firebaseViewModel: FirebaseViewModel by viewModels()        //'androidx.activity:activity-ktx:1.3.1'
    val adapter = AdsRcAdapter(this)
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    lateinit var filterLauncher: ActivityResultLauncher<Intent>
    private var clearUpdate: Boolean = true
    private var currentCategory: String? = null
    private var filter: String = "empty"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initRecyclerView()
        initViewModel()
        //firebaseViewModel.loadAllAds("0")
        bottomMenuOnClick()
        scrollListener()

    }

    private fun init() {
        currentCategory = getString(R.string.all_ads)
        onActivityResult()
        setSupportActionBar(binding.mainContent.toolbar)        //уведомляем систему что у нас вой тулбар
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.mainContent.toolbar, R.string.open, R.string.close)   //создаем кнопку
        binding.drawerLayout.addDrawerListener(toggle)                                                      //указываем, что наше меню (drawerLayout) будет
        //открываться по нажатию на эту кнопку
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener (this)                                            //наш navView будут передавать событие(нажатие)
        // сюда (в этот класс)
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)
        imAccount = binding.navView.getHeaderView(0).findViewById(R.id.imAccountImage)
        navViewSettings()
        onActivityResultFilter()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.id_filter) {
            val i = Intent(this@MainActivity, FilterActivity::class.java).apply {
                putExtra(FilterActivity.FILTER_KEY, filter)
            }
            filterLauncher.launch(i)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initRecyclerView(){
        binding.apply {
            //LayoutManager отвечает за позиционирование view-компонентов в RecyclerView, а также за определение того, когда следует переиспользовать view-компоненты, которые больше не видны пользователю.
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter
        }
    }

    private fun initViewModel(){
        firebaseViewModel.liveAdsData.observe(this, {
            //что происходит когда данные обновлены
            val list = getAdsByCategory(it)
            if (clearUpdate){
                adapter.updateAdapterWithClear(list)
            } else {
                adapter.updateAdapter(list)
            }
            binding.mainContent.tvEmpty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        })

    }

    private fun onActivityResult() {
        //новый колбэк, который будет получать данные когда мы выберем аккаунт
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account =
                    task.getResult(ApiException::class.java)      //следим зf ошибками, которые могут произойти во время регистрации или входа
                Log.d("MyLog", "Api 0")
                if (account != null) {
                    dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                Log.d("MyLog", "Api error: ${e.message}")
            }
        }

    }

    private fun onActivityResultFilter() {
        //новый колбэк, который будет получать данные когда мы выберем фильтр
        filterLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == RESULT_OK){
                filter = it.data?.getStringExtra(FilterActivity.FILTER_KEY)!!
                Log.d("MyLog", "Filter: $filter")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    //когда возвращаемся на экран
    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_home
    }



    private fun bottomMenuOnClick() = with(binding){
        mainContent.bNavView.setOnNavigationItemSelectedListener { item ->
            clearUpdate = true
            when(item.itemId){
                R.id.id_new_ad -> {
                    //запускаем новый акт; предаем контекст, на кот находимся, и акт на кот хотим перейти
                    val i = Intent(this@MainActivity,EditAdsAct::class.java)
                    startActivity(i)
                }
                R.id.id_my_ads -> {
                    firebaseViewModel.loadMyAds()
                    mainContent.toolbar.title = getString(R.string.my_ads)
                }
                R.id.id_favs -> {
                    firebaseViewModel.loadMyFavs()
                    mainContent.toolbar.title = getString(R.string.fav_ads)
                }
                R.id.id_home -> {
                    currentCategory = getString(R.string.all_ads)
                    firebaseViewModel.loadAllAdsFirstPage()
                    mainContent.toolbar.title = getString(R.string.all_ads)
                }
            }
            true
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {     //принимаем и обрабатываем нажатие на меню
        clearUpdate = true
        when (item.itemId) {
            R.id.id_my_ads -> {
                val myToast = Toast.makeText(this, "Pressed id_my_ads", Toast.LENGTH_SHORT)
                myToast.show()
            }
            R.id.id_car -> {
                getAdsByCat(getString(R.string.ad_car))
            }
            R.id.id_pc -> {
                getAdsByCat(getString(R.string.ad_pc))
            }
            R.id.id_smart -> {
                getAdsByCat(getString(R.string.ad_smartphone))
            }
            R.id.id_dm -> {
                getAdsByCat(getString(R.string.ad_dm))
            }
            R.id.id_sign_in -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
            }
            R.id.id_sign_up -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
            }
            R.id.id_sign_out -> {
                if (mAuth.currentUser?.isAnonymous == true) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                uiUpdate(null)
                mAuth.signOut()
                dialogHelper.accHelper.signOutG()
            }
            else -> {
                Toast.makeText(this, "Other", Toast.LENGTH_LONG).show()
            }

        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getAdsByCat(cat: String){
        currentCategory = cat
        //val catTime = "${cat}_0"
        firebaseViewModel.loadAllAdsByCat(cat)
    }

    fun uiUpdate(user:FirebaseUser?){
        if (user == null) {
            dialogHelper.accHelper.signInAnonymously(object: AccountHelper.Listener{
                override fun onComplete() {
                    tvAccount.setText(R.string.guest)       //либо tvAccount.text = getString(R.string.guest)
                    imAccount.setImageResource(R.drawable.ic_account_def)
                }

            })
        } else if (user.isAnonymous) {
            tvAccount.setText(R.string.guest)
            imAccount.setImageResource(R.drawable.ic_account_def)
        } else if (!user.isAnonymous) {
            tvAccount.text = user.email
            Picasso.get().load(user.photoUrl).into(imAccount)
        }
    }

    override fun onDeleteItem(ad: Ad) {
        firebaseViewModel.deleteItem(ad)
    }

    override fun onAdViewed(ad: Ad) {
        firebaseViewModel.adViewed(ad)
        val intent = Intent(this, DescriptionActivity::class.java)
        intent.putExtra("AD",ad)
        startActivity(intent)
    }

    override fun onFavClicked(ad: Ad) {
        firebaseViewModel.onFavClick(ad)
    }

    private fun navViewSettings() = with(binding){
        val menu = navView.menu
        val adsCat = menu.findItem(R.id.adsCat)
        val spanAdsCat = SpannableString(adsCat.title)
        spanAdsCat.setSpan(
            ForegroundColorSpan(
            ContextCompat.getColor(this@MainActivity, R.color.main_green_color)),
            0, adsCat.title.length, 0)
        adsCat.title = spanAdsCat

        val accCat = menu.findItem(R.id.accCat)
        val spanAccCat = SpannableString(accCat.title)
        spanAccCat.setSpan(ForegroundColorSpan(
            ContextCompat.getColor(this@MainActivity, R.color.main_green_color)),
            0, accCat.title.length, 0)
        accCat.title = spanAccCat
    }

    private fun scrollListener() = with(binding.mainContent) {
        rcView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d("MyLog", "Can't scroll down")
                    clearUpdate = false
                    val adsList = firebaseViewModel.liveAdsData.value!!
                    if (adsList.isNotEmpty()) {
                        getAdsByCatScroll(adsList)
                    }

                }
            }
        })
    }

    private fun getAdsByCatScroll(adsList: ArrayList<Ad>) {
        adsList[0].let {
            if (currentCategory == getString(R.string.all_ads)) {
                Log.d("MyLog", "Can't scroll down")
                firebaseViewModel.loadAllAdsNextPage(it.time)
            } else {
                val catTime = "${it.category}_${it.time}"
                firebaseViewModel.loadAllAdsByCatNextPage(catTime)
            }
        }
    }

    private fun getAdsByCategory(adList: ArrayList<Ad>): ArrayList<Ad>{
        val tempList = ArrayList<Ad>()
        tempList.addAll(adList)
        if (currentCategory != getString(R.string.all_ads)){
            tempList.clear()
            adList.forEach{
                if (it.category == currentCategory) tempList.add(it)
            }
        }
        tempList.reverse()
        return tempList
    }

    companion object{
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
    }
}