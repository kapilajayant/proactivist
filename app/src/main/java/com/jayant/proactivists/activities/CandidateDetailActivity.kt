package com.jayant.proactivists.activities

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
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
import com.jayant.proactivists.R
import com.jayant.proactivists.fragments.HelpFragment
import com.jayant.proactivists.models.Profile
import com.jayant.proactivists.models.ResponseModel
import com.jayant.proactivists.models.get_candidates.GetCandidatesItem
import com.jayant.proactivists.rest.APIService
import com.jayant.proactivists.rest.ApiUtils
import com.jayant.proactivists.utils.Constants
import com.jayant.proactivists.utils.DialogHelper
import com.ncorti.slidetoact.SlideToActView
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast
import androidx.core.net.toFile
import com.jayant.proactivists.fragments.NoInternetFragment
import com.jayant.proactivists.utils.Constants.NO_INTERNET
import com.jayant.proactivists.utils.NetworkManager
import okhttp3.MultipartBody

class CandidateDetailActivity : AppCompatActivity() {

    private val REQUEST_CODE_CONFIRM = 0
    lateinit var apiService: APIService
    private var allowDelete = false
    private lateinit var database: FirebaseDatabase
    private var referrerId: String? = null
    private lateinit var candidateId: String
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
    private lateinit var tv_summary: TextView
    private lateinit var tv_summary_static: TextView
    private lateinit var tv_skills: TextView
    private lateinit var iv_logo: ImageView
    private lateinit var iv_back: ImageView
    private lateinit var iv_help: ImageView
    private lateinit var iv_reject: CircleImageView
    private lateinit var iv_pending: CircleImageView
    private lateinit var iv_accept: CircleImageView
    private lateinit var relative_bg: RelativeLayout
    private lateinit var fl_skills: FlexboxLayout
    private lateinit var linear_status: LinearLayout
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var card_resume: CardView
    private lateinit var btn_reject: SlideToActView
    private lateinit var btn_accept: SlideToActView
    private lateinit var btn_action: Button
    private var profile: Profile? = null
    private var candidatesItem: GetCandidatesItem? = null
    private var profileId = ""
    private var applicationId = ""
    private var status = Constants.PENDING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_candidate_detail)

        apiService = ApiUtils.getAPIService()
        DialogHelper.showLoadingDialog(this)

        iv_profile = findViewById(R.id.iv_profile)
        tv_profile_name = findViewById(R.id.tv_profile_name)
        tvProfile = findViewById(R.id.tvProfile)
        tv_profile_mobile = findViewById(R.id.tv_profile_mobile)
        tv_profile_email = findViewById(R.id.tv_profile_email)
        tv_experience = findViewById(R.id.tv_experience)
        tv_experience_static = findViewById(R.id.tv_experience_static)
        tv_company_name = findViewById(R.id.tv_company_name)
        tvPosition = findViewById(R.id.tv_position)
        tv_position_static = findViewById(R.id.tv_position_static)
        iv_logo = findViewById(R.id.iv_logo)
        iv_back = findViewById(R.id.iv_back)
        iv_reject = findViewById(R.id.iv_reject)
        iv_pending = findViewById(R.id.iv_pending)
        iv_accept = findViewById(R.id.iv_accept)
        fl_skills = findViewById(R.id.fl_skills)
        tv_summary = findViewById(R.id.tv_summary)
        tv_summary_static = findViewById(R.id.tv_summary_static)
        tv_skills = findViewById(R.id.tv_skills)
        card_resume = findViewById(R.id.card_resume)
        swipe = findViewById(R.id.swipe)
        linear_status = findViewById(R.id.linear_status)
        relative_bg = findViewById(R.id.relative_bg)
        btn_reject = findViewById(R.id.btn_reject)
        btn_accept = findViewById(R.id.btn_accept)
        btn_action = findViewById(R.id.btn_action)

        iv_help = findViewById(R.id.iv_edit)
        iv_help.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_help))
        iv_back.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_back_black))
//        linear_status.visibility = View.VISIBLE

        val window = this.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.yellow)
        }
        relative_bg.background = ColorDrawable(ContextCompat.getColor(this, R.color.yellow))

        setUpWidgets()

        swipe.setOnRefreshListener {
            DialogHelper.showLoadingDialog(this)
            swipe.isRefreshing = true
            setUpWidgets(false)
            getApplicationById(candidateId, applicationId)
        }
    }

    private fun setUpWidgets(flag: Boolean = true) {

        fl_skills.removeAllViewsInLayout()

        iv_help.visibility = View.VISIBLE
        btn_accept.visibility = View.VISIBLE
        btn_reject.visibility = View.VISIBLE

        iv_help.setOnClickListener {
            val fragment = HelpFragment()
            fragment.show(supportFragmentManager, "")
        }

        iv_back.setOnClickListener {
            finish()
        }

        if (intent.hasExtra("candidate")) {
            intent.extras?.let {
                candidatesItem = it.getParcelable("candidate")
                applicationId = candidatesItem?.application_id ?: ""
                candidatesItem?.can_gid?.let {
                    profileId = it
                }
                candidatesItem?.status?.let {
                    status = it
                }
            }
        }

        if (intent.hasExtra("profileId")) {
            intent.extras?.let {
                profileId = it.getString("profileId", "")
            }
        }

        if (intent.hasExtra("application_id")) {
            intent.extras?.let {
                applicationId = it.getString("application_id", "")
            }
        }

        if(flag) {
            if (intent.hasExtra("status")) {
                intent.extras?.let {
                    status = it.getInt("status", Constants.PENDING)
                }
            }
            changeStatus(status, false)
        }

        candidateId = profileId

        if(NetworkManager.getConnectivityStatusString(this@CandidateDetailActivity) != NO_INTERNET) {
            getProfile(profileId, Constants.CANDIDATES_USER_TYPE)
        }
        else{
            val fragment = NoInternetFragment()
            fragment.show(supportFragmentManager, "")
        }

        btn_accept.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener{
            override fun onSlideComplete(view: SlideToActView) {
                if(NetworkManager.getConnectivityStatusString(this@CandidateDetailActivity) != NO_INTERNET){
                    updateApplication(Constants.ACCEPTED)
                }
                else{
                    val fragment = NoInternetFragment()
                    fragment.show(supportFragmentManager, "")
                }
            }
        }

        btn_reject.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener{
            override fun onSlideComplete(view: SlideToActView) {
                if(NetworkManager.getConnectivityStatusString(this@CandidateDetailActivity) != NO_INTERNET){
                    updateApplication(Constants.REJECTED)
                }
                else {
                    val fragment = NoInternetFragment()
                    fragment.show(supportFragmentManager, "")
                }
            }
        }

    }

    private fun changeStatus(status: Int, allow: Boolean = true) {

        val mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        currentUser?.uid?.let { referrerId ->
            this.referrerId = referrerId
        }
        when (status) {
            Constants.REJECTED -> {
                showRejectedUi()
            }
            Constants.PENDING -> {
                showPendingUi()
            }
            Constants.ACCEPTED -> {
                showAcceptedUi()
            }
            Constants.SUBMITTED -> {
                showSubmittedUi()
            }
            Constants.COMPLETED -> {
                showCompletedUi()
            }
        }

//        if(allow) {
//            val mAuth = FirebaseAuth.getInstance()
//            val currentUser = mAuth.currentUser
//            database = FirebaseDatabase.getInstance()
//
//            currentUser?.uid?.let { referrerId ->
//                this.referrerId = referrerId
//                if (status != 0) {
//                    updateApplication(status)
//                }
//            }
//        }

    }

    private fun updateApplication(status: Int) {

        val jsonObject = JSONObject()
        jsonObject.put("status", status.toString())
        jsonObject.put("can_gid", candidateId)
        jsonObject.put("application_id", applicationId)
        jsonObject.put("ref_gid", referrerId)
        val body = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            jsonObject.toString()
        )
        if (NetworkManager.getConnectivityStatusString(this) != NO_INTERNET) {
            apiService.updateApplication(body).enqueue(object : Callback<ResponseModel> {
                override fun onResponse(
                    call: Call<ResponseModel>,
                    response: Response<ResponseModel>
                ) {
                    if (response.isSuccessful) {
                        try {
                            if(response.code() == 200) {
                                when (status) {
                                    Constants.ACCEPTED -> {
                                        showAcceptedUi()
                                    }
                                    Constants.REJECTED -> {
                                        showRejectedUi()
                                    }
                                    Constants.SUBMITTED -> {
                                        showSubmittedUi()
                                    }
                                    Constants.COMPLETED -> {
                                        showCompletedUi()
                                    }
                                }
                            }
                            else {
                                Toast.makeText(
                                    this@CandidateDetailActivity,
                                    response.body()?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            Log.d("profile_backend", "onResponse: ${response.body()?.data}")
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
        else {
                val fragment = NoInternetFragment()
                fragment.show(supportFragmentManager, "")
            }
    }

    private fun addNewChip(skill: String, chipGroup: FlexboxLayout) {
        val chip = Chip(this@CandidateDetailActivity)
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

    private fun getProfileFromFirebase(profileId: String) {
        val mAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = mAuth.currentUser

        val database = FirebaseDatabase.getInstance()
        if (currentUser != null) {
            database.getReference("users").child(profileId).child("profile")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val value = snapshot.getValue(Profile::class.java)
                        if (value != null) {
                            profile = value

//                            setProfile(profile, candidate)

                            swipe.isRefreshing = false
                            Log.d("hui", "onDataChange: $profile")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                        Toast.makeText(
                            this@CandidateDetailActivity,
                            "Something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                        swipe.isRefreshing = false
                    }
                })
        }
    }

    private fun getApplicationById(uid: String, applicationId: String) {
        apiService.getApplicationById(uid, applicationId)
            .enqueue(object : Callback<ResponseModel> {
                override fun onResponse(
                    call: Call<ResponseModel>,
                    response: Response<ResponseModel>
                ) {
                    if (response.isSuccessful) {
                        try {
                            if(response.code() == 200) {
                                response.body()?.getApplicationStatusResponse()?.status?.let {
                                    status = it.toInt()
                                    changeStatus(status, false)
                                }
                            }
                            else {
                                Toast.makeText(
                                    this@CandidateDetailActivity,
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
                                    setProfile(it)
                                }
                            }
                            else {
                                Toast.makeText(
                                    this@CandidateDetailActivity,
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

    private fun setProfile(profile: Profile) {

        DialogHelper.hideLoadingDialog()

        swipe.isRefreshing = false

        if (profile.role == Constants.REFERRER) {
            card_resume.visibility = View.GONE
            tv_experience_static.visibility = View.GONE
            tvPosition?.text = profile.position
        } else {
            tvPosition?.visibility = View.GONE
            tv_position_static?.visibility = View.GONE
        }

        val currentUser = FirebaseAuth.getInstance().currentUser

        tv_profile_name.text = profile.name
        tv_profile_mobile.text = profile.phone
        tv_profile_email.text = profile.email
        tv_company_name.text = profile.company_name

        try {
            Glide.with(this@CandidateDetailActivity).load(profile.photo)
                .placeholder(ContextCompat.getDrawable(this, R.drawable.ic_profile))
                .into(iv_profile)

            Glide.with(this@CandidateDetailActivity).load(profile.company_logo)
                .into(iv_logo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        iv_logo.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(profile.company_linkedin))
            startActivity(intent)
        }

        if (profile.role == Constants.REFERRER) {
            fl_skills.visibility = View.GONE
            tv_summary.visibility = View.GONE
            tv_experience.visibility = View.GONE
            tv_skills.visibility = View.GONE
            tv_summary_static.visibility = View.GONE
        } else {
            tv_experience.text = profile.experience
            tv_summary.text = profile.about
            val type = object : TypeToken<ArrayList<String>>() {}.type
            val skills = Gson().fromJson<ArrayList<String>>(profile.skills, type)
            skills?.let {
                it.forEach { skill ->
                    addNewChip(skill, fl_skills)
                }
            }
        }
        card_resume.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(profile.resume))
                startActivity(intent)
            }
            catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    private fun showAcceptedUi(){
        val window = this.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.green)
        }
        relative_bg.background = ColorDrawable(ContextCompat.getColor(this, R.color.green))
        btn_action.text = "Upload Photo"
        btn_action.backgroundTintList = ContextCompat.getColorStateList(this, R.color.black)
        btn_accept.visibility = View.GONE
        btn_action.visibility = View.VISIBLE
        btn_action.setOnClickListener {
            val intent = Intent(this@CandidateDetailActivity, ConfirmActivity::class.java)
            intent.putExtra("ref", referrerId)
            intent.putExtra("can", candidateId)
            intent.putExtra("application_id", applicationId)
            startActivityForResult(intent, REQUEST_CODE_CONFIRM)
        }
    }

    private fun showPendingUi() {
        val window = this.window
        btn_accept.visibility = View.VISIBLE
        btn_reject.visibility = View.VISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.yellow)
        }
        relative_bg.background = ColorDrawable(ContextCompat.getColor(this, R.color.yellow))
    }

    private fun showRejectedUi() {

        val window = this.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.red)
        }
        btn_accept.visibility = View.GONE
        btn_reject.visibility = View.GONE
        relative_bg.background = ColorDrawable(ContextCompat.getColor(this, R.color.red))
    }

    private fun showSubmittedUi(){
        val window = this.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.purple_200)
        }
        relative_bg.background = ColorDrawable(ContextCompat.getColor(this, R.color.purple_200))
        btn_accept.visibility = View.GONE
        btn_reject.visibility = View.GONE
        btn_action.visibility = View.VISIBLE
        btn_action.text = "Submitted"
        btn_action.backgroundTintList = ContextCompat.getColorStateList(this, R.color.purple_200)
        btn_action.setOnClickListener {
            Toast.makeText(this, "Thank you for submitting the referral. We're just verifying the status from our end", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCompletedUi(){
        val window = this.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.blue)
        }
        relative_bg.background = ColorDrawable(ContextCompat.getColor(this, R.color.blue))
        btn_accept.visibility = View.GONE
        btn_reject.visibility = View.GONE
        btn_action.visibility = View.VISIBLE
        btn_action.text = "Complete"
        btn_action.backgroundTintList = ContextCompat.getColorStateList(this, R.color.blue)
        btn_action.setOnClickListener {
            Toast.makeText(this, "Referral process is complete", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_CONFIRM){
            if(data != null && data.hasExtra("confirmed")) {
                if (data.extras?.getBoolean("confirmed", false) == true) {
                    showSubmittedUi()
                }
            }
        }
    }

}