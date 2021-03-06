package com.jayant.proactivist.activities

import android.Manifest.permission
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.jayant.proactivist.BuildConfig
import com.jayant.proactivist.R
import com.jayant.proactivist.SliderAdapter
import com.jayant.proactivist.fragments.NoInternetFragment
import com.jayant.proactivist.fragments.SelectUserRoleFragment
import com.jayant.proactivist.fragments.StatusFragment
import com.jayant.proactivist.models.Profile
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.DialogHelper
import com.jayant.proactivist.utils.NetworkManager
import com.jayant.proactivist.utils.PrefManager
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class WelcomeActivity : AppCompatActivity() {

    lateinit var apiService: APIService
    private val requestPermissionCode = 123
    private var mAuth: FirebaseAuth? = null
    private val TAG = WelcomeActivity::class.java.simpleName
    private var viewPager: ViewPager? = null
    private var dotsIndicator: DotsIndicator? = null
    private var adapter: FragmentPagerAdapter? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val RC_SIGN_IN = 123
    private val dialog: ProgressDialog? = null


    override fun onStart() {
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        apiService = ApiUtils.getAPIService()
//        if (checkPermission()) {
//            Log.d("StartScreen", "Permissions are there")
//        } else {
//            requestPermission()
//        }

        mAuth = FirebaseAuth.getInstance()

        viewPager = findViewById(R.id.viewPager)
        dotsIndicator = findViewById(R.id.dots_indicator)
        adapter = SliderAdapter(supportFragmentManager, this@WelcomeActivity)
        viewPager?.adapter = adapter
        viewPager?.let {
            dotsIndicator?.setViewPager(it)
        }
    }


    fun checkPermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(applicationContext, permission.WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this@WelcomeActivity,
            arrayOf(permission.WRITE_EXTERNAL_STORAGE, permission.RECORD_AUDIO),
            requestPermissionCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestPermissionCode) {
            if (grantResults.size > 0) {
                val StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (StoragePermission && RecordPermission) {
                    val toast =
                        Toast.makeText(
                            this@WelcomeActivity,
                            "Permission Granted",
                            Toast.LENGTH_SHORT
                        )
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                } else {
                    val toast =
                        Toast.makeText(
                            this@WelcomeActivity,
                            "Permission Denied",
                            Toast.LENGTH_SHORT
                        )
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                    showSettingsDialog()
                }
            }
        }
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this@WelcomeActivity)
        builder.setTitle("Need Permissions")
        builder.setCancelable(false)
        builder.setMessage("This app needs audio permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS") { dialog: DialogInterface, which: Int ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, which: Int ->
            dialog.cancel()
            builder.show()
        }
        builder.show()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    fun getStarted(view: View) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInIntent: Intent? = mGoogleSignInClient?.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                DialogHelper.showLoadingDialog(this)
                dialog?.setMessage("Please wait...")
                dialog?.setCancelable(false)
                dialog?.show()
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                Log.d("log_google", "onActivityResult: " + account.email)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                e.printStackTrace()
                DialogHelper.hideLoadingDialog()
                // Google Sign In failed, update UI appropriately
                Log.d("log_google", "Google sign in failed", e)
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this,
                OnCompleteListener { task: Task<AuthResult?> ->
                    if (task.isSuccessful) {
                        dialog?.dismiss()
                        val user: FirebaseUser? = mAuth?.getCurrentUser()
                        user?.uid?.let {
                            if(NetworkManager.getConnectivityStatusString(this@WelcomeActivity) != Constants.NO_INTERNET) {
                                checkUpdate(it)
                            }
                            else{
                                val fragment = NoInternetFragment()
                                fragment.show(supportFragmentManager, "")
                            }
                        }
                    } else {
                        Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_LONG).show()
                    }
                })
    }

    private fun checkUpdate(uid: String) {

        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("current_version")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val updatedVersion = dataSnapshot.getValue(Int::class.java)
                val currentVersion = BuildConfig.VERSION_CODE
                if (updatedVersion != null) {
                    if(updatedVersion > currentVersion){
                        DialogHelper.hideLoadingDialog()
                        val fragment = StatusFragment(Constants.UPDATE)
                        fragment.show(supportFragmentManager, "")
                    }
                    else{
                        checkUser(uid)
                    }
                }
                else{
                    checkUser(uid)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                checkUser(uid)
            }
        })

    }

    private fun checkUser(uid: String) {
        val prefManager = PrefManager(this)
        prefManager.uid = uid
        apiService.checkUser(uid).enqueue(object : Callback<ResponseModel> {
            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                DialogHelper.hideLoadingDialog()
                if (response.isSuccessful) {
                    try {
                        if(response.code() == 200) {
                            val data = response.body()?.getCheckUserResponse()
                            if (data?.is_exist == true) {
                                val fragment = SelectUserRoleFragment(data.user_type)
                                fragment.show(supportFragmentManager, "")
                            }
                            else {
                                val i = Intent(this@WelcomeActivity, UserRoleActivity::class.java)
                                startActivity(i)
                                finish()
                            }
                        }
                        else{
                            Toast.makeText(this@WelcomeActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        DialogHelper.hideLoadingDialog()
                        e.printStackTrace()
                    }
                }
                else{
                    try {
                        val errorResponse = Gson().fromJson(response.errorBody()?.charStream(), ResponseModel::class.java)
                        Toast.makeText(this@WelcomeActivity, errorResponse.message, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                DialogHelper.hideLoadingDialog()
                t.printStackTrace()
            }
        })
    }
}