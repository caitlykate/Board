
package com.caitlykate.bulletinboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.caitlykate.bulletinboard.act.EditAdsAct
import com.caitlykate.bulletinboard.adapters.AdsRcAdapter
import com.caitlykate.bulletinboard.data.Ad
import com.caitlykate.bulletinboard.database.DBManager
import com.caitlykate.bulletinboard.database.ReadDataCallback
import com.caitlykate.bulletinboard.databinding.ActivityMainBinding
import com.caitlykate.bulletinboard.dialoghelper.DialogConst
import com.caitlykate.bulletinboard.dialoghelper.DialogHelper
import com.caitlykate.bulletinboard.dialoghelper.GoogleAccConst
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ReadDataCallback {
    private lateinit var rootElement: ActivityMainBinding                       //вместо lateinit можно было ActivityMainBinding? = null
                                                                                //rootElement - binding
    private val dialogHelper = DialogHelper(this)
    val mAuth = Firebase.auth                                      //Или чтобы получить обьект FirebaseAuth можно вызвать
                                                                   //статический метод FirebaseAuth.getInstance()
    private lateinit var tvAccount: TextView
    val dbManager = DBManager(this)
    val adapter = AdsRcAdapter(mAuth)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityMainBinding.inflate(layoutInflater)
        setContentView(rootElement.root)
        init()
        initRecyclerView()
        dbManager.readDataFromDb()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {        //Когда меню открывается впервые
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {           //когда нажимают на элемент меню
        if (item.itemId == R.id.new_add){
            //запускаем новый акт; предаем контекст, на кот находимся, и акт на кот хотим перейти
            val i = Intent(this,EditAdsAct::class.java)
            startActivity(i)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == GoogleAccConst.GOOGLE_SIGN_IN_REQUEST_CODE){
            Log.d("MyLog", "Sign in result")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)      //следим зf ошибками, которые могут произойти во время регистрации или входа
                Log.d("MyLog", "Api 0")
                if (account != null){
                    dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
                }
            } catch (e:ApiException){
                Log.d("MyLog", "Api error: ${e.message}")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    private fun init() {
        setSupportActionBar(rootElement.mainContent.toolbar)        //уведомляем систему что у нас вой тулбар
        val toggle = ActionBarDrawerToggle(this, rootElement.drawerLayout, rootElement.mainContent.toolbar, R.string.open, R.string.close)   //создаем кнопку
        rootElement.drawerLayout.addDrawerListener(toggle)                                                      //указываем, что наше меню (drawerLayout) будет
                                                                                                                //открываться по нажатию на эту кнопку
        toggle.syncState()
        rootElement.navView.setNavigationItemSelectedListener (this)                                            //наш navView будут передавать событие(нажатие)
                                                                                                                // сюда (в этот класс)
        tvAccount = rootElement.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)
    }

    private fun initRecyclerView(){
        rootElement.apply {
            //LayoutManager отвечает за позиционирование view-компонентов в RecyclerView, а также за определение того, когда следует переиспользовать view-компоненты, которые больше не видны пользователю.
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {                                            //принимаем и обрабатываем нажатие на меню
        when (item.itemId) {
            R.id.id_my_ads -> {
                val myToast = Toast.makeText(this, "Pressed id_my_ads", Toast.LENGTH_SHORT)
                myToast.show()
            }
            R.id.id_car -> {
                Toast.makeText(this, "Pressed id_car", Toast.LENGTH_LONG).show()
            }
            R.id.id_pc -> {
                Toast.makeText(this, "Pressed id_pc", Toast.LENGTH_LONG).show()
            }
            R.id.id_smart -> {
                Toast.makeText(this, "Pressed id_smart", Toast.LENGTH_LONG).show()
            }
            R.id.id_dm -> {
                Toast.makeText(this, "Pressed id_dm", Toast.LENGTH_LONG).show()
            }
            R.id.id_sign_in -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
            }
            R.id.id_sign_up -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
            }
            R.id.id_sign_out -> {
                uiUpdate(null)
                mAuth.signOut()
                dialogHelper.accHelper.signOutG()
            }
            else -> {
                Toast.makeText(this, "Other", Toast.LENGTH_LONG).show()
            }

        }

        rootElement.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun uiUpdate(user:FirebaseUser?){
        tvAccount.text = if (user == null) {
            resources.getString(R.string.not_reg)
        } else {
            user.email
        }
    }

    override fun readData(list: List<Ad>) {
        adapter.updateAdapter(list)
    }
}