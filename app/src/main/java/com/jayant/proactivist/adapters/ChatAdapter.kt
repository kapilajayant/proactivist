package com.jayant.proactivist.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jayant.proactivist.R
import com.jayant.proactivist.models.ChatItem
import com.jayant.proactivist.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatAdapter(var context: Context, var chatList: ArrayList<ChatItem>): RecyclerView.Adapter<ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view: View = if(viewType == Constants.RECEIVER){
            LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false)
        } else{
            LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false)
        }
        return ChatViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return chatList[position].type
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        try {

            val date = Date()

            chatList[position].time.toLong().let { it1 -> date.time = it1 }

            val formatter = SimpleDateFormat("hh:mm")
            holder.tv_message.text = chatList[position].message
            holder.tv_time.text = formatter.format(date)

            holder.chat_root.setOnClickListener {
                Toast.makeText(context, "Message Copied!", Toast.LENGTH_SHORT).show()
                val clip = ClipData.newPlainText("label", chatList[position].message)
                (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = chatList.size

    fun addChats(list: ArrayList<ChatItem>){
        chatList.addAll(0, list)
        notifyItemRangeInserted(0, list.size)
    }

}

public class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val chat_root = itemView.findViewById<RelativeLayout>(R.id.chat_root)
    val tv_message = itemView.findViewById<TextView>(R.id.tv_message)
    val tv_time = itemView.findViewById<TextView>(R.id.tv_time)
}