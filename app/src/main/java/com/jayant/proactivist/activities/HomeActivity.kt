package com.jayant.proactivist.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jayant.proactivist.BuildConfig
import com.jayant.proactivist.R
import com.jayant.proactivist.fragments.*
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.ForceUpdateChecker
import com.jayant.proactivist.utils.PrefManager

class HomeActivity : AppCompatActivity(), InviteCodeFragment.InviteCodeCallback {

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
        if(prefManager.showInviteCode){
            val fragment = InviteCodeFragment(this)
            fragment.show(supportFragmentManager, "")
        }

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
                R.id.menu_shop -> {
                    if (R.id.menu_shop !== selectedItemId) {
                        openFragment(ShopFragment())
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

    override fun addedInviteCode() {

    }

}