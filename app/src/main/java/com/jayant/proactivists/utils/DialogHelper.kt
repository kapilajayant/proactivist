package com.jayant.proactivists.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.jayant.proactivists.R

object DialogHelper {
    private var alertDialog: AlertDialog? = null

    fun showLoadingDialog(activity: Activity){
        val builder = AlertDialog.Builder(activity)
        val view: View = activity.layoutInflater.inflate(R.layout.dialog_loading, null)
        builder.setView(view)

        alertDialog = builder.create()

        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.setCanceledOnTouchOutside(true)
        alertDialog?.show()

    }

    fun hideLoadingDialog(){
        alertDialog?.dismiss()
    }

}