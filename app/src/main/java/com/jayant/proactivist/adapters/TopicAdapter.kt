package com.jayant.proactivist.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jayant.proactivist.activities.ArticleListActivity
import com.jayant.proactivist.R
import com.jayant.proactivist.fragments.NoInternetFragment
import com.jayant.proactivist.models.learn.TopicModel
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.NetworkManager

class TopicAdapter(val context: Context, val list: ArrayList<TopicModel>, val fragmentManager: FragmentManager): RecyclerView.Adapter<TopicViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_topic, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        holder.tv_topic.text = list[position].topic
        Glide.with(holder.itemView).load(list[position].image).placeholder(ContextCompat.getDrawable(context, R.drawable.ic_learn_placeholder)).into(holder.iv_topic)
        holder.card_topic.setOnClickListener {
            if (NetworkManager.getConnectivityStatusString(context) != Constants.NO_INTERNET) {
                val intent = Intent(context, ArticleListActivity::class.java)
                intent.putExtra("topic", list[position].topic)
                intent.putExtra("image", list[position].image)
                context.startActivity(intent)
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

class TopicViewHolder(view: View): RecyclerView.ViewHolder(view){

    val iv_topic = view.findViewById<ImageView>(R.id.iv_topic)
    val tv_topic = view.findViewById<TextView>(R.id.tv_topic)
    val card_topic = view.findViewById<CardView>(R.id.card_topic)

}