package com.jayant.proactivist.activities

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.jayant.proactivist.BuildConfig
import com.jayant.proactivist.R
import com.jayant.proactivist.fragments.NoInternetFragment
import com.jayant.proactivist.fragments.StatusFragment
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SplashActivity : AppCompatActivity() {

    private val UPDATE_REQUEST_CODE = 12
    lateinit var apiService: APIService
    private var mAuth: FirebaseAuth? = null
    private val TAG = SplashActivity::class.java.simpleName
    private lateinit var prefManager: PrefManager
    var appUpdateManager: AppUpdateManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        var inviteCode: String?
        val data: Uri? = intent.data
        inviteCode = data?.getQueryParameter("inviteCode")
        prefManager = PrefManager(this)
        prefManager.inviteCode = inviteCode
        if(inviteCode.isNullOrEmpty().not()){
            prefManager.showInviteCode = true
        }

        check()

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        val window = this.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#000000")
            window.decorView.systemUiVisibility = View.STATUS_BAR_VISIBLE
        }

    }

    private fun check() {
        try {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference("current_version")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val updatedVersion = dataSnapshot.getValue(Int::class.java)
                    val currentVersion = BuildConfig.VERSION_CODE
                    if (updatedVersion != null) {
                        if(updatedVersion > currentVersion){
                            val fragment = StatusFragment(Constants.UPDATE)
                            fragment.show(supportFragmentManager, "")
                        }
                        else{
                            updateNotAvailable()
                        }
                    }
                    else{
                        updateNotAvailable()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    updateNotAvailable()
                }
            })

            checkForUpdates()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkForUpdates() {

        val database = FirebaseDatabase.getInstance()
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager!!.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE // For a flexible update, use AppUpdateType.FLEXIBLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                try {
                    Toast.makeText(this@SplashActivity, "startedByCore", Toast.LENGTH_SHORT).show()
                    appUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this@SplashActivity,
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {

            }
        }
    }

    private fun updateNotAvailable() {

        apiService = ApiUtils.getAPIService()

        mAuth = FirebaseAuth.getInstance()

        val currentUser: FirebaseUser? = mAuth?.currentUser
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
                            }
                            else {
                                data?.user_type?.let {
                                    if(it != "both") {
//                                     if(prefManager.getProfile() != null)
                                        // check if pref manager has profile already
                                        val role = it
                                        prefManager.profileRole = role

                                        TokenManager.getToken(uid, role)
                                        getProfile(uid, role + "s")
                                    }
                                    else{
                                        val role = prefManager.profileRole
                                        if(role.isNullOrEmpty().not()){
                                            getProfile(uid, role + "s")
                                        }
                                        else{
                                            signOut()
                                        }
                                    }
                                }
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
                else{
                    try {
                        val errorResponse = Gson().fromJson(response.errorBody()?.charStream(), ResponseModel::class.java)
                        Toast.makeText(this@SplashActivity, errorResponse.message, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
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

    private fun getProfile(uid: String, userType: String) {
        apiService.getProfile(uid, userType)
            .enqueue(object : Callback<ResponseModel> {
                override fun onResponse(
                    call: Call<ResponseModel>,
                    response: Response<ResponseModel>
                ) {
                    if (response.isSuccessful) {
                        try {
                            if(response.code() == 200) {
                                response.body()?.getProfileResponse()?.let {
                                    ShortcutManagerUtil.createShortcut(this@SplashActivity)
                                    prefManager.setProfile(it)
                                    val i = Intent(this@SplashActivity, HomeActivity::class.java)
                                    startActivity(i)
                                    finish()
                                }
                            }
                            else {
                                Toast.makeText(
                                    this@SplashActivity,
                                    response.body()?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    else{
                        try {
                            val errorResponse = Gson().fromJson(response.errorBody()?.charStream(), ResponseModel::class.java)
                            Toast.makeText(this@SplashActivity, errorResponse.message, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                    Log.d("profile_backend", "onFailure: ${call.request().url()}")
                    t.printStackTrace()
                }

            })
    }

    private fun signOut() {

        val mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mGoogleSignInClient.signOut().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                ShortcutManagerUtil.removeShortcuts(this@SplashActivity)
                mAuth.signOut()
                PrefManager(this).clear()
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            else {
                Toast.makeText(this, "Sign out failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}