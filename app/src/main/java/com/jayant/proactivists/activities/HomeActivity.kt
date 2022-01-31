package com.jayant.proactivists.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jayant.proactivists.BuildConfig
import com.jayant.proactivists.R
import com.jayant.proactivists.fragments.*
import com.jayant.proactivists.utils.Constants
import com.jayant.proactivists.utils.ForceUpdateChecker
import com.jayant.proactivists.utils.PrefManager

class HomeActivity : AppCompatActivity() {

    var navView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ForceUpdateChecker(this, supportFragmentManager)

        navView = findViewById(R.id.nav_view)

        navView?.selectedItemId = R.id.menu_jobs

        navView?.setOnNavigationItemSelectedListener(navigationItemSelectedListener)
        openFragment(ReferFragment.newInstance("", ""))

        val prefManager = PrefManager(this)
        val currentVersionCode = BuildConfig.VERSION_CODE
        val latestVersionCode = prefManager.latestVersionCode

        if(currentVersionCode < latestVersionCode){
            val fragment = StatusFragment(Constants.UPDATE)
            fragment.show(supportFragmentManager, "")
        }

    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private var navigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            val selectedItemId = navView?.selectedItemId
            when (item.itemId) {
                R.id.menu_feed -> {
                    if (R.id.menu_feed !== selectedItemId) {
                        openFragment(HomeFragment.newInstance("", ""))
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.menu_learn -> {
                    if (R.id.menu_learn !== selectedItemId) {
                        openFragment(LearnFragment.newInstance("", ""))
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.menu_jobs -> {
                    if (R.id.menu_jobs !== selectedItemId) {
                        openFragment(ReferFragment.newInstance("", ""))
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.menu_settings -> {
                    if (R.id.menu_settings !== selectedItemId) {
                        openFragment(SettingsFragment.newInstance("", ""))
                    }
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onBackPressed() {
        Log.d("hui", "onBackPressed: ")
        val selectedItemId = navView?.selectedItemId
        if(selectedItemId == R.id.menu_jobs) {
            Log.d("hui", "onBackPressed if: ")
            finish()
        }
        else{
            Log.d("hui", "onBackPressed else: ")
            navView?.selectedItemId = R.id.menu_jobs
        }
    }

}