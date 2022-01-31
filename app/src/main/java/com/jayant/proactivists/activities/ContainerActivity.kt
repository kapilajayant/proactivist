package com.jayant.proactivists.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.jayant.proactivists.R
import com.jayant.proactivists.fragments.BackPressListener
import com.jayant.proactivists.fragments.EditProfileFragment
import com.jayant.proactivists.fragments.UserRoleFragment
import com.jayant.proactivists.models.Profile

class ContainerActivity : AppCompatActivity(), UserRoleFragment.UserRoleSelected,
    BackPressListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        if(intent.hasExtra("edit") and intent.hasExtra("profile") and (intent.extras?.getBoolean("edit") == true)){
            val profile = intent.extras?.getParcelable<Profile>("profile")
            openFragment(profile?.let { profile.role?.let { it1 ->
                EditProfileFragment(profile,
                    it1
                )
            } })
        }
        else{
            openFragment(UserRoleFragment())
        }

    }

    private fun openFragment(fragment: Fragment?) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment!!)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun userRoleSelected(role: String) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.remove(UserRoleFragment())
        openFragment(EditProfileFragment(role))
    }

    override fun backPressed() {
        Log.d("Edit", "backPressed: ")
        finish()
    }

}