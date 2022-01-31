package com.jayant.proactivist.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jayant.proactivist.R
import com.jayant.proactivist.models.JobItemModel
import com.jayant.proactivist.utils.Constants.REFERRER
import com.jayant.proactivist.utils.PrefManager
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class JobDetailsActivity : AppCompatActivity() {

    private lateinit var fl_skills: FlexboxLayout
    private lateinit var tv_company_name: TextView
    private lateinit var tv_job_title: TextView
    private lateinit var tv_experience: TextView
    private lateinit var tv_summary: TextView
    private lateinit var tv_location: TextView
    private lateinit var iv_logo: ImageView
    private lateinit var btn_apply: Button
    private lateinit var tv_app_bar: TextView
    private lateinit var iv_back: CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)

        fl_skills = findViewById(R.id.fl_skills)
        tv_company_name = findViewById(R.id.tv_company_name)
        tv_job_title = findViewById(R.id.tv_job_title)
        tv_experience = findViewById(R.id.tv_experience)
        tv_summary = findViewById(R.id.tv_summary)
        tv_location = findViewById(R.id.tv_location)
        iv_logo = findViewById(R.id.iv_logo)
        btn_apply = findViewById(R.id.btn_apply)

        tv_app_bar = findViewById(R.id.tv_app_bar)
        iv_back = findViewById(R.id.iv_back)

        tv_app_bar.text = "Job Details"
        iv_back.setOnClickListener {
            finish()
        }

        val job = intent.getParcelableExtra<JobItemModel>("job")

        val mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        val prefManager = PrefManager(this)
        if(prefManager.profileRole == REFERRER){
            btn_apply.text = "View Applicants"

            btn_apply.setOnClickListener {
                val database = FirebaseDatabase.getInstance()
                val job_id = job?.job_id
                job?.job_poster?.let { it1 ->
                    if (job_id != null) {
                        val intent = Intent(this, ViewApplicantsActivity::class.java)
                        intent.putExtra("job", job)
                        startActivity(intent)
                    }
                }
            }
        }
        else{
            btn_apply.setOnClickListener {
                val database = FirebaseDatabase.getInstance()
                val job_id = job?.job_id
                job?.job_poster?.let { it1 ->
                    if (job_id != null) {
                        val date = Date()
                        database.reference.child("users").child(it1).child("jobs").child(job_id).child(date.time.toString()).setValue(currentUser?.uid)
                        currentUser?.uid?.let { it2 -> database.reference.child("users").child(it2).child("jobs").child(job_id).child(date.time.toString()).setValue(
                            it2
                        )}
                        Snackbar.make(btn_apply, "Job posted!", Snackbar.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }

        tv_company_name.text = job?.company_name
        tv_job_title.text = job?.job_title
        tv_experience.text = "Experience required: ${job?.experience} years"
        tv_summary.text = job?.summary
        if(job?.location != null){
            tv_location.text = job.location
        }
        else{
            tv_location.visibility = View.GONE
        }

        Log.d("hui", "onCreate: " + job?.company_linkedin)

        Glide.with(this).load(job?.company_logo).into(iv_logo)


        iv_logo.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(job?.company_linkedin))
            startActivity(intent)
        }

        val type = object : TypeToken<ArrayList<String>>() {}.type
        val skills = Gson().fromJson<ArrayList<String>>(job?.skills, type)
        skills.forEach {
            addNewChip(it, fl_skills)
        }
    }

    private fun addNewChip(skill: String, chipGroup: FlexboxLayout) {
        val chip = Chip(this@JobDetailsActivity)
        val layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        layoutParams.setMargins(0, 0, 10, 0)
        chip.text = skill
        chip.isCloseIconEnabled = false
        chip.isClickable = true
        chip.isCheckable = false
        chipGroup.addView(chip as View, chipGroup.childCount - 1, layoutParams)
    }
}