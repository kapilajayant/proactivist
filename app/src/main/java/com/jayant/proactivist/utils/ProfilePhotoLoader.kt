package com.jayant.proactivist.utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

object ProfilePhotoLoader{

    fun load(context: Context, url: String?, name: String?, imageView: ImageView, textView: TextView){

        if(!url.isNullOrEmpty()){
            Glide.with(context).load(url).apply(RequestOptions.circleCropTransform()).into(imageView)
        }
        else{
            imageView.visibility = View.GONE
            textView.visibility = View.VISIBLE
            textView.text = name
        }

    }

}