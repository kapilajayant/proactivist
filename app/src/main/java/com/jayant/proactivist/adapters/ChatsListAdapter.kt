package com.jayant.proactivist.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jayant.proactivist.R
import com.jayant.proactivist.activities.ChatActivity
import com.jayant.proactivist.models.Profile
import com.jayant.proactivist.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView

class ChatsListAdapter(var context: Context, var chatList: ArrayList<Profile>): RecyclerView.Adapter<ChatListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.card_layout_profile, parent, false)
        return ChatListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        try {

            holder.tvDesc.visibility = View.GONE
            holder.tvTitle.text = chatList[position].name
            Glide.with(context).load(chatList[position].photo).placeholder(ContextCompat.getDrawable(context, R.drawable.ic_profile)).into(holder.ivLogo)

            holder.cardProfile.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra(Constants.RECEIVER_PROFILE, chatList[position])
                context.startActivity(intent)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = chatList.size

}

public class ChatListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tvTitle = itemView.findViewById<TextView>(R.id.tv_title)
    val ivLogo = itemView.findViewById<CircleImageView>(R.id.iv_logo)
    val tvDesc = itemView.findViewById<TextView>(R.id.tv_desc)
    val cardProfile = itemView.findViewById<CardView>(R.id.card_profile)

}