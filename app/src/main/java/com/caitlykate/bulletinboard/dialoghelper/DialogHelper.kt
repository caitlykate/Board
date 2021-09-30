package com.caitlykate.bulletinboard.dialoghelper

import android.app.AlertDialog
import android.view.View
//import android.widget.TextView
import android.widget.Toast
import com.caitlykate.bulletinboard.MainActivity
import com.caitlykate.bulletinboard.R
import com.caitlykate.bulletinboard.accounthelper.AccountHelper
import com.caitlykate.bulletinboard.databinding.SignDialogBinding

class DialogHelper(val act: MainActivity) {
    val accHelper = AccountHelper(act)


    fun createSignDialog(index:Int) {
        val builder = AlertDialog.Builder(act)                                          //спец класс для создания диалогов, передаем контекст
        val rootDialogElement = SignDialogBinding.inflate(act.layoutInflater)           //диалог создается как обычный экран
        val view = rootDialogElement.root
        builder.setView(view)
        setDialogState(index,rootDialogElement)

        val dialog = builder.create()

        rootDialogElement.btGoogleSignIn.setOnClickListener{
            accHelper.signInWithGoogle()
            dialog.dismiss()
        }

        rootDialogElement.btSignUpIn.setOnClickListener{
            setOnClickSignUpIn(index, rootDialogElement, dialog)
        }

        rootDialogElement.btForgetP.setOnClickListener{
            setOnClickResetPassword(rootDialogElement, dialog)
        }

        dialog.show()
    }

    private fun setOnClickResetPassword(rootDialogElement: SignDialogBinding, dialog: AlertDialog?) {
        if (rootDialogElement.edSignEmail.text.isNotEmpty()){
            act.mAuth.sendPasswordResetEmail(rootDialogElement.edSignEmail.text.toString()).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(act, R.string.email_reset_password_was_sent, Toast.LENGTH_LONG).show()
                    }
                }
                dialog?.dismiss()
            } else {
                rootDialogElement.tvDialogMessage.visibility = View.VISIBLE
            }

        }

    private fun setOnClickSignUpIn(index: Int, rootDialogElement: SignDialogBinding, dialog: AlertDialog?) {
        dialog?.dismiss()           //dismiss will start if the dialog isn't null
        if (index == DialogConst.SIGN_UP_STATE){
            accHelper.signUpWithEmail(rootDialogElement.edSignEmail.text.toString(),
                    rootDialogElement.edSignPassword.text.toString())
        } else {
            accHelper.signInWithEmail(rootDialogElement.edSignEmail.text.toString(),
                    rootDialogElement.edSignPassword.text.toString())
        }
    }

    private fun setDialogState(index: Int, rootDialogElement: SignDialogBinding) {
        if (index == DialogConst.SIGN_UP_STATE) {
            rootDialogElement.tvSignTitle.text = act.resources.getString(R.string.ac_sign_up)
            rootDialogElement.btSignUpIn.text = act.resources.getString(R.string.sign_up_act)
        }
        else {

            rootDialogElement.tvSignTitle.text = act.resources.getString(R.string.ac_sign_in)       //ресурсы пришли из активити?
            rootDialogElement.btSignUpIn.text = act.resources.getString(R.string.sign_in_act)
            rootDialogElement.btForgetP.visibility = View.VISIBLE
        }
    }
}