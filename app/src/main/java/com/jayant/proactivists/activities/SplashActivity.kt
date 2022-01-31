package com.jayant.proactivists.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jayant.proactivists.R
import com.jayant.proactivists.fragments.NoInternetFragment
import com.jayant.proactivists.models.ResponseModel
import com.jayant.proactivists.rest.APIService
import com.jayant.proactivists.rest.ApiUtils
import com.jayant.proactivists.utils.Constants
import com.jayant.proactivists.utils.NetworkManager
import com.jayant.proactivists.utils.UpdateManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {

    private val UPDATE_REQUEST_CODE = 12
    lateinit var apiService: APIService
    private var mAuth: FirebaseAuth? = null
    private val TAG = SplashActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        check()

        val window = this.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#000000")
            window.decorView.systemUiVisibility = View.STATUS_BAR_VISIBLE
        }

    }

    private fun checkUser(uid: String) {
        apiService.checkUser(uid).enqueue(object : Callback<ResponseModel> {
            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                if (response.isSuccessful) {
                    try {
                        if(response.code() == 200) {
                            val data = response.body()?.getCheckUserResponse()
                            if (data?.is_exist == false) {
                                val i = Intent(this@SplashActivity, UserRoleActivity::class.java)
                                startActivity(i)
                                finish()
                            } else {
                                val i = Intent(this@SplashActivity, HomeActivity::class.java)
                                startActivity(i)
                                finish()
                            }
                        }
                        else{
                            Toast.makeText(this@SplashActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Toast.makeText(this@SplashActivity, "Couldn't connect to servers!", Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }
        })
    }

    private fun check() {

        try {
            val appUpdateManager = AppUpdateManagerFactory.create(this@SplashActivity)

            // Returns an intent object that you use to check for an update.
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo

            // Checks that the platform will allow the specified type of update.
            appUpdateInfoTask.addOnSuccessListener {
                    appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    Toast.makeText(this, "Update available", Toast.LENGTH_SHORT).show()
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        UpdateManager.UPDATE_REQUEST_CODE
                    )
                }
                else{
                    Toast.makeText(this, "Update not available", Toast.LENGTH_SHORT).show()
                    updateNotAvailable()
                }
            }
            appUpdateInfoTask.addOnFailureListener {
                it.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateNotAvailable() {

        apiService = ApiUtils.getAPIService()

        mAuth = FirebaseAuth.getInstance()

        val currentUser: FirebaseUser? = mAuth?.currentUser
        Handler().postDelayed({

            if (currentUser != null) {
                if(NetworkManager.getConnectivityStatusString(this@SplashActivity) != Constants.NO_INTERNET) {
                    checkUser(currentUser.uid)
                }
                else{
                    val fragment = NoInternetFragment()
                    fragment.show(supportFragmentManager, "")
                }
            }
            else {
                val i = Intent(this, WelcomeActivity::class.java)
                startActivity(i)
                finish()
            }

        }, 1500)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.e("update", "Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
            else{
                Toast.makeText(this@SplashActivity, "Update done!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}