package com.jayant.proactivist.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jayant.proactivist.R
import com.jayant.proactivist.adapters.ChatAdapter
import com.jayant.proactivist.models.ChatItem
import com.jayant.proactivist.models.Profile
import com.jayant.proactivist.utils.Constants
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {
    private var userId: String? = ""
    private lateinit var progressBar: ProgressBar
    private lateinit var tv_app_bar: TextView
    private lateinit var iv_back: ImageView
    private lateinit var iv_profile: ImageView
    private lateinit var et_chat: EditText
    private lateinit var btn_send: ImageButton
    private lateinit var rv_chats: RecyclerView
    private var chatList = ArrayList<ChatItem>()
    private lateinit var chatAdapter: ChatAdapter
    private var receiverId: String? = ""
    private val PAGE_SIZE = 20
    private var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        userId = FirebaseAuth.getInstance().currentUser?.uid

        var profile: Profile? = null
        var receiverName: String? = null
        var receiverPhoto: String? = null

        if (intent.hasExtra(Constants.RECEIVER_PROFILE)){
            profile = intent.getParcelableExtra(Constants.RECEIVER_PROFILE)
        }

        if (profile != null) {
            receiverId = profile.uid
            receiverName = profile.name
            receiverPhoto = profile.photo
        }

        progressBar = findViewById(R.id.progressBar)
        tv_app_bar = findViewById(R.id.tv_app_bar)
        iv_back = findViewById(R.id.iv_back)
        iv_profile = findViewById(R.id.iv_profile_chat)
        btn_send = findViewById(R.id.btn_send)
        et_chat = findViewById(R.id.et_chat)
        rv_chats = findViewById(R.id.rv_chats)

        iv_profile.visibility = View.VISIBLE
        Glide.with(this).load(receiverPhoto).apply(RequestOptions.circleCropTransform()).into(iv_profile)
        tv_app_bar.text = receiverName
        iv_back.setOnClickListener {
            finish()
        }
        tv_app_bar.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("load_profile_role", profile?.role)
            intent.putExtra("load_profile_uid", profile?.uid)
            startActivity(intent)
        }
        iv_profile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("load_profile_role", profile?.role)
            intent.putExtra("load_profile_uid", profile?.uid)
            startActivity(intent)
        }

        receiverId?.let { fetchChats(it) }

        btn_send.setOnClickListener {
            if (!et_chat.text.isNullOrEmpty()) {
                userId?.let { senderId ->
                    receiverId?.let { receiverId ->
                        val date = Date()
                        val chatItemReceiver = ChatItem(
                            et_chat.text.toString(),
                            date.time.toString(),
                            Constants.RECEIVER
                        )
                        FirebaseDatabase.getInstance().reference.child("chat")
                            .child(receiverId)
                            .child(senderId)
                            .push().setValue(chatItemReceiver)
                        val chatItem = ChatItem(
                            et_chat.text.toString(),
                            date.time.toString(),
                            Constants.SENDER
                        )
                        FirebaseDatabase.getInstance().reference.child("chat")
                            .child(senderId)
                            .child(receiverId)
                            .push().setValue(chatItem)
                        et_chat.setText("")
                    }
                }
            }
        }

        chatAdapter = ChatAdapter(this@ChatActivity, chatList)
        rv_chats.apply {
            adapter = chatAdapter
            val layoutManager = LinearLayoutManager(this@ChatActivity, RecyclerView.VERTICAL, false)
            layoutManager.stackFromEnd = true
            this.layoutManager = layoutManager
        }
        rv_chats.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (linearLayoutManager != null && linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    page++
                    progressBar.visibility = View.VISIBLE
                    receiverId?.let { loadMore(it) }
                }
            }
        })

    }

    private fun loadMore(receiverId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = ArrayList<ChatItem>()
                for (item in snapshot.children) {
                    val chat = item.getValue(ChatItem::class.java)
                    if (chat != null && !chatList.contains(chat)) {
                        tempList.add(chat)
                    }
                }
                progressBar.visibility = View.GONE
                chatAdapter.addChats(tempList)
            }

            override fun onCancelled(error: DatabaseError) {

                progressBar.visibility = View.GONE
            }
        }
        if (userId != null) {

            val reference = FirebaseDatabase.getInstance().reference
                .child("chat").child(userId)
                .child(receiverId).limitToLast(page * PAGE_SIZE)
            reference.addValueEventListener(listener)
        }
    }

    private fun fetchChats(receiverId: String) {
                progressBar.visibility = View.VISIBLE
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (item in snapshot.children) {
                    val chat = item.getValue(ChatItem::class.java)
                    if (chat != null && !chatList.contains(chat)) {
                        chatList.add(chat)
                    }
                }
//                chatAdapter.notifyItemRangeInserted(
//                    chatAdapter.chatList.size,
//                    (chatAdapter.chatList.size + snapshot.childrenCount).toInt()
//                )
                chatAdapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
                rv_chats.smoothScrollToPosition(chatList.size)
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
            }
        }
        if (userId != null) {
            val reference = FirebaseDatabase.getInstance().reference
                .child("chat").child(userId)
                .child(receiverId).limitToLast(page * PAGE_SIZE)
            reference.addValueEventListener(listener)
        }
    }
}