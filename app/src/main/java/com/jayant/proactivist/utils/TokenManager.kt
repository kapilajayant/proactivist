package com.jayant.proactivist.utils

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

object TokenManager {

    fun getToken(uid: String, role: String){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("Refer", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            val database = FirebaseDatabase.getInstance()
            database.reference.child(role + "s").child(uid).child("profile").child("token").setValue(token)
            Log.d("Refer", token)
        })
    }
}