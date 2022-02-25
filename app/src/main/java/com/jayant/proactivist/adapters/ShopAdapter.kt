package com.jayant.proactivist.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.jayant.proactivist.R
import com.jayant.proactivist.models.ShopModel

class ShopAdapter(val context: Context, val list: ArrayList<ShopModel>): RecyclerView.Adapter<ShopViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_item_shop, parent, false)
        return ShopViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        holder.tv_product_name.text = list[position].product_name
        holder.tv_coins.text = list[position].product_coins
        Glide.with(holder.itemView).load(list[position].product_image).placeholder(ContextCompat.getDrawable(context, R.drawable.ic_shop)).into(holder.iv_product)
        holder.card_product.setOnClickListener {
            Snackbar.make(holder.itemView, "Sold out", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}

class ShopViewHolder(view: View): RecyclerView.ViewHolder(view){

    val iv_product = view.findViewById<ImageView>(R.id.iv_product)
    val tv_product_name = view.findViewById<TextView>(R.id.tv_product_name)
    val tv_coins = view.findViewById<TextView>(R.id.tv_coins)
    val card_product = view.findViewById<CardView>(R.id.card_product)

}