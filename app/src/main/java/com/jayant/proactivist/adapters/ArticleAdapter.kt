package com.jayant.proactivist.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jayant.proactivist.R
import com.jayant.proactivist.fragments.NoInternetFragment
import com.jayant.proactivist.models.learn.ArticleModel
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.NetworkManager

class ArticleAdapter(val context: Context, val list: ArrayList<ArticleModel>, val imageTopic: String, val fragmentManager: FragmentManager): RecyclerView.Adapter<ArticleViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.tv_article.text = list[position].title
        Glide.with(holder.itemView).load(imageTopic).placeholder(ContextCompat.getDrawable(context, R.drawable.ic_learn_placeholder)).into(holder.iv_topic)
        holder.card_article.setOnClickListener {
            if (NetworkManager.getConnectivityStatusString(context) != Constants.NO_INTERNET) {
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(context, Uri.parse(list[position].url))
            }
            else {
                val fragment = NoInternetFragment()
                fragment.show(fragmentManager, "")
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}

class ArticleViewHolder(view: View): RecyclerView.ViewHolder(view){

    val iv_topic = view.findViewById<ImageView>(R.id.iv_topic)
    val tv_article = view.findViewById<TextView>(R.id.tv_article)
    val card_article = view.findViewById<CardView>(R.id.card_article)

}