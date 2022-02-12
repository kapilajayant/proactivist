package com.jayant.proactivist.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jayant.proactivist.R
import com.jayant.proactivist.fragments.NoInternetFragment
import com.jayant.proactivist.models.Profile
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.utils.*
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private lateinit var iv_profile: CircleImageView
    private lateinit var tvProfile: TextView
    private lateinit var tv_profile_name: TextView
    private lateinit var tv_profile_mobile: TextView
    private lateinit var tv_profile_email: TextView
    private lateinit var tv_experience: TextView
    private lateinit var tv_experience_static: TextView
    private lateinit var tv_company_name: TextView
    private var tvPosition: TextView? = null
    private var tv_position_static: TextView? = null
    private lateinit var tv_summary: ScrollableTextView
    private lateinit var tv_summary_static: TextView
    private lateinit var tv_skills: TextView
    private lateinit var iv_linkedin: ImageView
    private lateinit var iv_logo: ImageView
    private lateinit var iv_back: ImageView
    private lateinit var iv_edit: ImageView
    private lateinit var fl_skills: FlexboxLayout
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var card_resume: CardView

    lateinit var apiService: APIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        apiService = ApiUtils.getAPIService()

        val window = this.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.purple_500)
            window.decorView.systemUiVisibility = View.STATUS_BAR_VISIBLE
        }

        swipe = findViewById(R.id.swipe)
        swipe.setOnRefreshListener {
            swipe.isRefreshing = true
            setUpWidgets()
        }

    }

    override fun onResume() {
        super.onResume()
        setUpWidgets()
    }

    private fun setUpWidgets() {

        iv_profile = findViewById<CircleImageView>(R.id.iv_profile)
        tv_profile_name = findViewById<TextView>(R.id.tv_profile_name)
        tvProfile = findViewById<TextView>(R.id.tvProfile)
        tv_profile_mobile = findViewById<TextView>(R.id.tv_profile_mobile)
        tv_profile_email = findViewById<TextView>(R.id.tv_profile_email)
        tv_experience = findViewById<TextView>(R.id.tv_experience)
        tv_experience_static = findViewById<TextView>(R.id.tv_experience_static)
        tv_company_name = findViewById(R.id.tv_company_name)
        tvPosition = findViewById(R.id.tv_position)
        tv_position_static = findViewById(R.id.tv_position_static)
        iv_logo = findViewById<ImageView>(R.id.iv_logo)
        iv_linkedin = findViewById(R.id.iv_linkedin)
        iv_back = findViewById<CircleImageView>(R.id.iv_back)
        iv_edit = findViewById(R.id.iv_edit)
        fl_skills = findViewById(R.id.fl_skills)
        tv_summary = findViewById<ScrollableTextView>(R.id.tv_summary)
        tv_summary_static = findViewById<TextView>(R.id.tv_summary_static)
        tv_skills = findViewById<TextView>(R.id.tv_skills)
        card_resume = findViewById(R.id.card_resume)

        fl_skills.removeAllViewsInLayout()

        if (intent.hasExtra("profile")){
            // loading received profile

            DialogHelper.showLoadingDialog(this)
            setProfile(intent.getParcelableExtra("profile"))
        }
        else if(intent.hasExtra("load_profile_role")){
            // loading profile
            val role = intent.extras?.getString("load_profile_role", "")
            val uid = intent.extras?.getString("load_profile_uid", "")
            if (uid != null && role != null) {
                if(NetworkManager.getConnectivityStatusString(this@ProfileActivity) != Constants.NO_INTERNET) {
                    DialogHelper.showLoadingDialog(this)
                    getProfile(uid, role)
                }
                else{
                    val fragment = NoInternetFragment()
                    fragment.show(supportFragmentManager, "")
                }
            }
        }
        else{
            // loading own profile
            FirebaseAuth.getInstance().currentUser?.uid?.let { PrefManager(this).profileRole?.let { it1 ->
                if(NetworkManager.getConnectivityStatusString(this@ProfileActivity) != Constants.NO_INTERNET) {
                    DialogHelper.showLoadingDialog(this)
                    getProfile(it, it1)
                }
                else{
                    val fragment = NoInternetFragment()
                    fragment.show(supportFragmentManager, "")
                }
            } }
        }

        iv_back.setOnClickListener {
            finish()
        }

    }

    private fun getProfile(uid: String, profileRole: String) {
        apiService.getProfile(uid, profileRole+"s")
            .enqueue(object : Callback<ResponseModel> {
                override fun onResponse(
                    call: Call<ResponseModel>,
                    response: Response<ResponseModel>
                ) {
                    if (response.isSuccessful) {
                        try {
                            if(response.code() == 200) {
                                swipe.isRefreshing = false
                                response.body()?.getProfileResponse()?.let {
                                    val prefManager = PrefManager(this@ProfileActivity)
                                    prefManager.setProfile(it)
                                    setProfile(it)
                                }
                            }
                            else {
                                Toast.makeText(
                                    this@ProfileActivity,
                                    response.body()?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
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

    private fun addNewChip(skill: String, chipGroup: FlexboxLayout) {
        val chip = Chip(this@ProfileActivity)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 10, 0)
        chip.text = skill
        chip.isCloseIconEnabled = false
        chip.isClickable = true
        chip.isCheckable = false
        chipGroup.addView(chip as View, chipGroup.childCount - 1, layoutParams)
    }

    private fun getProfileFromFirebase() {
        val mAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = mAuth.currentUser
        var profile: Profile? = null

        val database = FirebaseDatabase.getInstance()
        if (currentUser != null) {
            database.getReference("users").child(currentUser.uid).child("profile")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val value = snapshot.getValue(Profile::class.java)
                        if (value != null) {
                            val prefManager = PrefManager(this@ProfileActivity)
                            prefManager.setProfile(value)
                            profile = value

                            setProfile(profile)

                            swipe.isRefreshing = false
                            Log.d("hui", "onDataChange: $profile")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                        Toast.makeText(this@ProfileActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                        swipe.isRefreshing = false
                    }
                })
        }
    }

    private fun setProfile(profile: Profile?) {
        DialogHelper.hideLoadingDialog()
        iv_edit.setOnClickListener {
            val i = Intent(this, EditProfileActivity::class.java)
            i.putExtra("fromEdit", true)
            i.putExtra("role", profile?.role)
            i.putExtra("profile", profile)
            startActivity(i)
        }
        if(profile?.role == Constants.REFERRER){
            card_resume.visibility = View.GONE
            tv_experience_static.visibility = View.GONE
            tvPosition?.text = profile.position
        }
        else{
            tvPosition?.visibility = View.GONE
            tv_position_static?.visibility = View.GONE
        }

        val currentUser = FirebaseAuth.getInstance().currentUser

        if(profile?.email != currentUser?.email){
            iv_edit.visibility = View.GONE
        }

        tv_profile_name.text = profile?.name
        tv_profile_mobile.text = profile?.phone
        tv_profile_email.text = profile?.email
        tv_company_name.text = profile?.company_name

        try {

            ProfilePhotoLoader.load(
                this,
                profile?.photo,
                profile?.name?.subSequence(0, 1).toString(),
                iv_profile,
                tvProfile
            )

            Glide.with(this@ProfileActivity).load(profile?.company_logo)
                .apply(RequestOptions.circleCropTransform())
                .into(iv_logo)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        iv_logo.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(profile?.company_linkedin))
            startActivity(intent)
        }
        iv_linkedin.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(profile?.personal_linkedin))
                startActivity(intent)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if(profile?.role == Constants.REFERRER){
            fl_skills.visibility = View.GONE
            tv_experience.visibility = View.GONE
            tv_skills.visibility = View.GONE
            if(profile.about.isNullOrEmpty()){
                tv_summary_static.visibility = View.GONE
                tv_summary.visibility = View.GONE
            }
            else{
                tv_summary.text = profile.about
            }
        }
        else{
            val years = profile?.experience?.substringBefore(":")
            val months = profile?.experience?.substringAfter(":")
            tv_experience.text = "$years years $months months"
            tv_summary.text = profile?.about
            val type = object : TypeToken<ArrayList<String>>() {}.type
            val skills = Gson().fromJson<ArrayList<String>>(profile?.skills, type)
            skills?.let {
                it.forEach { skill ->
                    addNewChip(skill, fl_skills)
                }
            }
        }
        card_resume.setOnClickListener {
            try {
                if(!profile?.resume.isNullOrEmpty())
                {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(profile?.resume))
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this, "Invalid google drive link", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid google drive link", Toast.LENGTH_SHORT).show()
            }
        }
    }

}