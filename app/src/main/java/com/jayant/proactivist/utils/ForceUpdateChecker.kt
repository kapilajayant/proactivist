package com.jayant.proactivist.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.jayant.proactivist.BuildConfig
import com.jayant.proactivist.fragments.StatusFragment

class ForceUpdateChecker(var context: Context, var supportFragmentManager: FragmentManager) {
    private val TAG = "ForceUpdate"
    private val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    private val firebaseDefaultMap = mutableMapOf<String, Any>()
    private val VERSION_CODE_KEY = "latest_app_version"

    init {

        firebaseDefaultMap[VERSION_CODE_KEY] = BuildConfig.VERSION_CODE
        mFirebaseRemoteConfig.setDefaultsAsync(firebaseDefaultMap)
        mFirebaseRemoteConfig.setConfigSettingsAsync(FirebaseRemoteConfigSettings.Builder().build())
        mFirebaseRemoteConfig.fetch().addOnCompleteListener {
            if(it.isSuccessful){
                mFirebaseRemoteConfig.activate().addOnCompleteListener {
                    Log.d(TAG, "Fetched value: " + mFirebaseRemoteConfig.getString(VERSION_CODE_KEY))
                    checkForUpdate()
                }
            }
            else{
                Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        }

        Log.d(TAG, "Default value: " + mFirebaseRemoteConfig.getString(VERSION_CODE_KEY));
    }

    private fun checkForUpdate() {
        val latestAppVersion = mFirebaseRemoteConfig.getDouble(VERSION_CODE_KEY).toInt()
        if (latestAppVersion > getCurrentVersionCode()) {
            val fragment = StatusFragment(Constants.UPDATE)
            fragment.show(supportFragmentManager, "")
        }
    }

    private fun getCurrentVersionCode():Int {
        try {
            return BuildConfig.VERSION_CODE
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace();
        }
        return -1;
    }
}