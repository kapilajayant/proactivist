package com.jayant.proactivist.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.jayant.proactivist.R
import com.jayant.proactivist.adapters.ChatsListAdapter
import com.jayant.proactivist.fragments.NoInternetFragment
import com.jayant.proactivist.models.Profile
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.DialogHelper
import com.jayant.proactivist.utils.NetworkManager
import com.jayant.proactivist.utils.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatListActivity : AppCompatActivity() {

    private lateinit var rv_chats: RecyclerView
    private lateinit var tv_app_bar: TextView
    private lateinit var iv_back: ImageView
    private lateinit var linear_empty: LinearLayout
    private lateinit var chatListAdapter: ChatsListAdapter
    private var chatList = ArrayList<Profile>()
    lateinit var apiService: APIService
    private var role = Constants.REFERRER

    private var total = 1
    private var current = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        DialogHelper.showLoadingDialog(this)

        apiService = ApiUtils.getAPIService()

        val prefManager = PrefManager(this)
        if (prefManager.profileRole == Constants.REFERRER) {
            role = Constants.CANDIDATE
        }

        tv_app_bar = findViewById(R.id.tv_app_bar)
        iv_back = findViewById(R.id.iv_back)
        rv_chats = findViewById(R.id.rv_chats)
        linear_empty = findViewById(R.id.linear_empty)

        iv_back.setOnClickListener {
            finish()
        }

        tv_app_bar.text = "Chats"

        chatListAdapter = ChatsListAdapter(this@ChatListActivity, chatList)
        rv_chats.apply {
            adapter = chatListAdapter
            val layoutManager =
                LinearLayoutManager(this@ChatListActivity, RecyclerView.VERTICAL, false)
            layoutManager.stackFromEnd = true
            this.layoutManager = layoutManager
        }

        if (NetworkManager.getConnectivityStatusString(this@ChatListActivity) != Constants.NO_INTERNET) {
            getChatIds()
        } else {
            val fragment = NoInternetFragment()
            fragment.show(supportFragmentManager, "")
        }

    }

    private fun getChatIds() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                total = snapshot.childrenCount.toInt()
                for (item in snapshot.children) {
                    item.key?.let {
                        getProfile(it, role)
                    }
                }
                DialogHelper.hideLoadingDialog()
            }

            override fun onCancelled(error: DatabaseError) {
                DialogHelper.hideLoadingDialog()
            }
        }
        if (userId != null) {
            val reference = FirebaseDatabase.getInstance().reference
                .child("chat").child(userId)
            reference.addListenerForSingleValueEvent(listener)
        }
    }

    private fun getProfile(uid: String, profileRole: String) {
        apiService.getProfile(uid, profileRole + "s")
            .enqueue(object : Callback<ResponseModel> {
                override fun onResponse(
                    call: Call<ResponseModel>,
                    response: Response<ResponseModel>
                ) {
                    if (response.isSuccessful) {
                        try {
                            if(response.code() == 200) {
                                if (response.body()?.status == "success") {
                                    response.body()?.getProfileResponse()?.let {
                                        chatList.add(it)
                                        chatListAdapter.notifyDataSetChanged()
                                    }
                                    if (current == total) {
                                        if (chatList.isEmpty()) {
                                            linear_empty.visibility = View.VISIBLE
                                            rv_chats.visibility = View.GONE
                                        } else {
                                            rv_chats.visibility = View.VISIBLE
                                            linear_empty.visibility = View.GONE
                                        }
                                    }
                                    current++
                                } else {
                                    if (current == total) {
                                        if (chatList.isEmpty()) {
                                            linear_empty.visibility = View.VISIBLE
                                            rv_chats.visibility = View.GONE
                                        } else {
                                            rv_chats.visibility = View.VISIBLE
                                            linear_empty.visibility = View.GONE
                                        }
                                    }
                                    current++
                                }
                            }
                            else {
                                Toast.makeText(
                                    this@ChatListActivity,
                                    response.body()?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            DialogHelper.hideLoadingDialog()
                            if(current == total){
                                if (chatList.isEmpty()) {
                                    linear_empty.visibility = View.VISIBLE
                                    rv_chats.visibility = View.GONE
                                }
                                else {
                                    rv_chats.visibility = View.VISIBLE
                                    linear_empty.visibility = View.GONE
                                }
                            }
                            current++
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                    Log.d("profile_backend", "onFailure: ${call.request().url()}")
                    t.printStackTrace()
                    DialogHelper.hideLoadingDialog()
                    if(current == total){
                        if (chatList.isEmpty()) {
                            linear_empty.visibility = View.VISIBLE
                            rv_chats.visibility = View.GONE
                        }
                        else {
                            rv_chats.visibility = View.VISIBLE
                            linear_empty.visibility = View.GONE
                        }
                    }
                    current++
                }
            })
    }
}