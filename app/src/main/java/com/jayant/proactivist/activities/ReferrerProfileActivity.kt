package com.jayant.proactivist.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.jayant.proactivist.R
import com.jayant.proactivist.fragments.NoInternetFragment
import com.jayant.proactivist.fragments.ReferralProfileFragment
import com.jayant.proactivist.models.Profile
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.models.get_referrers.GetReferrersItem
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.NetworkManager
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ReferrerProfileActivity : AppCompatActivity() {

    lateinit var apiService: APIService

    private lateinit var iv_profile: CircleImageView
    private lateinit var iv_linkedin: ImageView
    private lateinit var tv_profile_name: TextView
    private lateinit var tv_profile: TextView
    private lateinit var tv_about: TextView
    private lateinit var iv_back: CircleImageView
    private lateinit var tv_position: TextView
    private lateinit var card_ask: CardView
    private lateinit var swipe: SwipeRefreshLayout
    private var referrer: GetReferrersItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_referrer_profile)

        apiService = ApiUtils.getAPIService()

        val window = this.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.purple_500)
            window.decorView.systemUiVisibility = View.STATUS_BAR_VISIBLE
        }

        iv_profile = findViewById(R.id.iv_profile)
        iv_linkedin = findViewById(R.id.iv_linkedin)
        tv_profile_name = findViewById(R.id.tv_profile_name)
        tv_profile = findViewById(R.id.tv_profile)
        tv_position = findViewById(R.id.tv_position)
        tv_about = findViewById(R.id.tv_about)
        iv_back = findViewById(R.id.iv_back)
        card_ask = findViewById(R.id.card_ask)
        swipe = findViewById(R.id.swipe)

        iv_back.setOnClickListener {
            finish()
        }

        swipe.setOnRefreshListener {
            swipe.isRefreshing = true

            if (intent.hasExtra("referrer")){
                intent.extras?.let {
                    referrer = it.getParcelable("referrer")
                }
            }
            if(NetworkManager.getConnectivityStatusString(this@ReferrerProfileActivity) != Constants.NO_INTERNET) {
                getProfile()
            }
            else{
                val fragment = NoInternetFragment()
                fragment.show(supportFragmentManager, "")
            }

        }

        if (intent.hasExtra("referrer")){
            intent.extras?.let {
                referrer = it.getParcelable("referrer")
            }
        }
        if(NetworkManager.getConnectivityStatusString(this@ReferrerProfileActivity) != Constants.NO_INTERNET) {
            getProfile()
        }
        else{
            val fragment = NoInternetFragment()
            fragment.show(supportFragmentManager, "")
        }

    }

    private fun setProfile(profile: Profile) {

        tv_profile_name.text = profile.name
        tv_position.text = "${profile?.position} at ${profile?.company_name}"

        Glide.with(this).load(profile.photo).placeholder(ContextCompat.getDrawable(this, R.drawable.ic_profile)).into(iv_profile)

        if(profile.about.isNullOrEmpty()){
            tv_about.text = "${profile?.name} is ${profile?.position} at ${profile?.company_name}"
            if(!profile.experience.isNullOrEmpty()){
                tv_about.text = tv_about.text as String + " and has ${profile?.experience} years of experience"
            }
            tv_about.text = tv_about.text as String + ". Go ahead and ask ${profile.name?.substringBefore(" ")?.lowercase(Locale.getDefault())} for a referral."
        }
        else{
            tv_about.text = profile.about
        }
        iv_linkedin.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(profile.personal_linkedin))
                startActivity(intent)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
        card_ask.setOnClickListener {
            val fragment = profile.let { it1 -> profile.photo?.let { it2 ->
                ReferralProfileFragment(
                    it2, referrer)
            }
            }
            fragment?.show(supportFragmentManager, "")
        }
    }

    private fun getProfile() {
        referrer?.ref_gid?.let {uid ->
            apiService.getProfile(uid, Constants.REFERRER_USER_TYPE)
                .enqueue(object : Callback<ResponseModel> {
                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {
                        if (response.isSuccessful) {
                            try {
                                if (response.code() == 200) {
                                    response.body()?.getProfileResponse()?.let {
                                        swipe.isRefreshing = false
                                        setProfile(it)
                                    }
                                }
                                else {
                                    Toast.makeText(
                                        this@ReferrerProfileActivity,
                                        response.body()?.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                swipe.isRefreshing = false
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                        swipe.isRefreshing = false
                        Log.d("profile_backend", "onFailure: ${call.request().url()}")
                        t.printStackTrace()
                    }

                })
        }
    }
}