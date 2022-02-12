package com.jayant.proactivist.activities

import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.jayant.proactivist.R
import com.jayant.proactivist.fragments.InviteCodeFragment
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList


class CoinsActivity : AppCompatActivity(), InviteCodeFragment.InviteCodeCallback {

    private lateinit var iv_back: ImageView
    private lateinit var iv_others: ImageView
    private lateinit var iv_whatsapp: ImageView
    private lateinit var tv_code: TextView
    private lateinit var tv_coins: TextView
    private lateinit var tv_enter_code: TextView
    private var inviteCode = ""
    lateinit var apiService: APIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coins)

        apiService = ApiUtils.getAPIService()
        getCoins()
        val window = this.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.purple_500)
            window.decorView.systemUiVisibility = View.STATUS_BAR_VISIBLE
        }

        iv_back = findViewById(R.id.iv_back)
        iv_whatsapp = findViewById(R.id.iv_whatsapp)
        iv_others = findViewById(R.id.iv_others)
        tv_code = findViewById(R.id.tv_code)
        tv_coins = findViewById(R.id.tv_coins)
        tv_enter_code = findViewById(R.id.tv_enter_code)

        iv_back.setOnClickListener {
            finish()
        }

        iv_whatsapp.setOnClickListener {
            var message = "I'm inviting you to join Proactivist. \nGet 50 coins by using my invite code: \n\n\nhttps://proactivist.in?inviteCode=$inviteCode \n\n\n Download the app now https://play.google.com/store/apps/details?id=com.jayant.proactivist"
            val isWhatsappInstalled: Boolean = whatsappInstalledOrNot("com.whatsapp")
            if (isWhatsappInstalled) {
                try {
                    shareWhatsapp(message)
                }
                catch (e: Exception){
                    e.printStackTrace()
                }
            }
            else{
                val uri = Uri.parse("market://details?id=com.whatsapp")
                val goToMarket = Intent(Intent.ACTION_VIEW, uri)
                Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT).show()
                startActivity(goToMarket)
            }
        }

        iv_others.setOnClickListener {
            var message = "I'm inviting you to join Proactivist. \nGet 50 coins by using my invite code: \n\n\nhttps://proactivist.in?inviteCode=$inviteCode \n\n\n Download the app now https://play.google.com/store/apps/details?id=com.jayant.proactivist"
            shareOthers(message)
        }

        tv_code.setOnClickListener {
            Toast.makeText(this, "Invite code copied!", Toast.LENGTH_SHORT).show()
            val clip = ClipData.newPlainText("label", inviteCode)
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
        }
        tv_enter_code.setOnClickListener {
            val fragment = InviteCodeFragment(this)
            fragment.show(supportFragmentManager, "")
        }
    }

    private fun shareWhatsapp(message: String) {
        val sendIntent = Intent()
        val arrayList = ArrayList<Uri>()
        arrayList.add(message.toUri())
        arrayList.add(inviteCode.toUri())
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, message)
        sendIntent.type = "text/plain"
//        sendIntent.putParcelableArrayListExtra(Intent.EXTRA_TEXT, arrayList)
        sendIntent.setPackage("com.whatsapp")
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Join Proactivist")
        sendIntent.component = ComponentName("com.whatsapp", "com.whatsapp.ContactPicker")
        if (sendIntent.resolveActivity(packageManager) != null) {
            startActivity(sendIntent)
        }
    }


    private fun whatsappInstalledOrNot(uri: String): Boolean {
        val pm = packageManager
        var app_installed = false
        app_installed = try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return app_installed
    }

    private fun shareOthers(message: String){
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Join Proactivist")
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            message
        )
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, "choose one"))
    }

    private fun getCoins() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            apiService.getCoins(uid).enqueue(object : Callback<ResponseModel> {
                override fun onResponse(
                    call: Call<ResponseModel>,
                    response: Response<ResponseModel>
                ) {
                    if (response.isSuccessful) {
                        try {
                            if(response.code() == 200){
                                response.body()?.getCoinsResponse()?.coins.let {
                                    tv_coins.text = it.toString()
                                }
                                response.body()?.getCoinsResponse()?.invite_code.let {
                                    inviteCode = it.toString()
                                    tv_code.text = inviteCode
                                }
                                response.body()?.getCoinsResponse()?.already_invited.let {
                                    if(it == true)
                                    tv_enter_code.visibility = View.GONE
                                }
                            }
                            else {
                                Toast.makeText(this@CoinsActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@CoinsActivity, "Something went wrong!", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                    Toast.makeText(this@CoinsActivity, "Something went wrong!", Toast.LENGTH_SHORT).show()
                    Log.d("profile_backend", "onFailure: ${call.request().url()}")
                    t.printStackTrace()
                }

            })
        }
    }

    override fun addedInviteCode() {
        getCoins()
    }

}