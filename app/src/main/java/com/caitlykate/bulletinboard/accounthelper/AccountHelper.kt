package com.caitlykate.bulletinboard.accounthelper

import android.util.Log
import android.widget.Toast
import com.caitlykate.bulletinboard.MainActivity
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.constants.FirebaseAuthConst
import com.caitlykate.bulletinboard.dialoghelper.GoogleAccConst
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*


class AccountHelper(act: MainActivity) {                            //из активити нам нужен firebase authentication
    private val act = act
    private lateinit var signInClient: GoogleSignInClient

    fun signUpWithEmail(email:String, password:String){
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->

                if (task.isSuccessful){
                    sendEmailVerification(task.result?.user!!)
                    act.uiApdate(task.result?.user)
                } else {
                    Toast.makeText(act, R.string.sign_up_error, Toast.LENGTH_LONG).show()
                    Log.d("MyLog", "Exception: " + task.exception)
                    if (task.exception is FirebaseAuthUserCollisionException){
                        val exception = task.exception as FirebaseAuthUserCollisionException
                        //Log.d("MyLog", "Exception.errorCode: ${exception.errorCode}" )
                        if (exception.errorCode == FirebaseAuthConst.ERROR_EMAIL_ALREADY_IN_USE){
                            Toast.makeText(act, FirebaseAuthConst.ERROR_EMAIL_ALREADY_IN_USE, Toast.LENGTH_LONG).show()
                            //соединяем email и g-acc
                            linkEmailToG(email,password)

                        }
                    } else if (task.exception is FirebaseAuthInvalidCredentialsException){
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == FirebaseAuthConst.ERROR_INVALID_EMAIL){
                            //просим ввести корректно
                            Toast.makeText(act, FirebaseAuthConst.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG).show()
                        }
                    }
                    if (task.exception is FirebaseAuthWeakPasswordException){
                        val exception = task.exception as FirebaseAuthUserCollisionException
                        //Log.d("MyLog", "Exception.errorCode: ${exception.errorCode}" )
                        if (exception.errorCode == FirebaseAuthConst.ERROR_WEAK_PASSWORD){
                            Toast.makeText(act, FirebaseAuthConst.ERROR_WEAK_PASSWORD, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    fun signInWithEmail(email:String, password:String){
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->

                if (task.isSuccessful){
                    Toast.makeText(act, R.string.sign_in_success, Toast.LENGTH_LONG).show()
                    act.uiApdate(task.result?.user)
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == FirebaseAuthConst.ERROR_WRONG_PASSWORD) {
                            //ПОЛЬЗОВАТЕЛЯ С ТАКИМ ИМЕНЕМ И ПАРОЛЕМ НЕ СУЩЕСТВУЕТ
                            Toast.makeText(
                                act,
                                FirebaseAuthConst.ERROR_WRONG_PASSWORD,
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            if (exception.errorCode == FirebaseAuthConst.ERROR_INVALID_EMAIL) {
                                //НЕВЕРНЫЙ ФОРМАТ ЕМАЙЛА
                                Toast.makeText(
                                        act,
                                        FirebaseAuthConst.ERROR_INVALID_EMAIL,
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    if (task.exception is FirebaseAuthInvalidUserException){
                        val exception = task.exception as FirebaseAuthInvalidUserException
                        //Log.d("MyLog", "Exception.errorCode: ${exception.errorCode}" )
                        if (exception.errorCode == FirebaseAuthConst.ERROR_USER_NOT_FOUND) {
                            //ТАКОЙ ПОЛЬЗОВАТЕЛЬ НЕ ЗАРЕГИСТРИРОВАН
                            Toast.makeText(
                                    act,
                                    FirebaseAuthConst.ERROR_USER_NOT_FOUND,
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                        Log.d("MyLog", "Exception: ${task.exception}" )
                    Toast.makeText(act, R.string.sign_in_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }



    fun sendEmailVerification(user:FirebaseUser){
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful){
                Toast.makeText(act, R.string.send_verification_done, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(act, R.string.send_verification_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    //функции для регистрации по гугл-аккаунту

    private fun getSignInClient(): GoogleSignInClient{     //через этот класс создаем интент, чтобы отправить запрос к системе
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                    requestIdToken(act.getString(R.string.default_web_client_id)).requestEmail().build() //default_web_client_id чтобы система понимала, что это приложение с утройства
        //теперь эти опции нужно передать в наш  GoogleSignInClient
        return GoogleSignIn.getClient(act,gso)
    }

    fun signInWithGoogle (){
        signInClient = getSignInClient()                //получаем настроенного клиента
        val intent = signInClient.signInIntent       //получаем интент для входа
        act.startActivityForResult(intent, GoogleAccConst.GOOGLE_SIGN_IN_REQUEST_CODE)     //запускаем интент для входа на mainAct
        //резульат нам вернет аккаунт и оттуда мы получим токен, чтобы зарегистрироваться на firebase
    }

    fun signOutG(){
        getSignInClient().signOut()
    }

    fun signInFirebaseWithGoogle(token: String){
        val credential = GoogleAuthProvider.getCredential(token, null)        //учетные данные
        act.mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful){
                Toast.makeText(act,"Sign in done", Toast.LENGTH_LONG).show()
                act.uiApdate(task.result?.user)
            }
        }
    }

    private fun linkEmailToG(email: String, password: String){
        val credential = EmailAuthProvider.getCredential(email,password)
        act.mAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                Toast.makeText(act,R.string.link_done, Toast.LENGTH_LONG).show()
            }
        }
    }
}