package com.jayant.proactivist.utils

import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener

object GuideManager {
    fun showGuide(context: Context, view: View, title: String, desc: String, count: Int = 0, listener: GuideListener){
        val prefManager = PrefManager(context)
        val guideCount = prefManager.guideCount
        if(view.isVisible.not()){
            listener.onDismiss(view)
        }
        if(count > guideCount && view.isVisible) {
            GuideView.Builder(context)
                .setTitle(title)
                .setContentText(desc)
                .setTargetView(view)
                .setDismissType(DismissType.outside)
                .setGuideListener {
                    listener.onDismiss(view)
                }
                .build()
                .show()
        }
    }
}