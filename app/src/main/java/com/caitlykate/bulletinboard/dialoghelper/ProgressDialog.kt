package com.caitlykate.bulletinboard.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import com.caitlykate.bulletinboard.databinding.ProgressDialogLayoutBinding
import com.caitlykate.bulletinboard.databinding.SignDialogBinding

object ProgressDialog {

    fun createProgressDialog(act: Activity): AlertDialog {
        val builder = AlertDialog.Builder(act)                                          //спец класс для создания диалогов, передаем контекст
        val rootDialogElement = ProgressDialogLayoutBinding.inflate(act.layoutInflater)           //диалог создается как обычный экран
        val view = rootDialogElement.root
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }

}