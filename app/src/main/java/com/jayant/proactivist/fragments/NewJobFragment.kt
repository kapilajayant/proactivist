package com.jayant.proactivist.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.jayant.proactivist.R
import com.jayant.proactivist.models.JobItemModel
import com.jayant.proactivist.utils.PrefManager
import android.view.MotionEvent
import android.view.View.OnTouchListener


class NewJobFragment : BottomSheetDialogFragment() {

    private lateinit var prefManager: PrefManager
    private var skillsList = arrayListOf<String>()
    private var et_skills: EditText? = null
    private var et_job_title: EditText? = null
    private var et_company_name: EditText? = null
    private var et_experience: EditText? = null
    private var et_summary: EditText? = null
    private var et_location: EditText? = null
    private lateinit var card_post: CardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.layout_new_job_posting, container, false)

        et_company_name = view.findViewById(R.id.et_company_name)
        et_job_title = view.findViewById(R.id.et_job_title)
        et_experience = view.findViewById(R.id.et_experience)
        et_summary = view.findViewById(R.id.et_summary)
        et_skills = view.findViewById(R.id.et_skills)
        card_post = view.findViewById(R.id.card_post)
        et_location = view.findViewById(R.id.et_location)
        val iv_logo: ImageView = view.findViewById(R.id.iv_logo)

        val fl_skills = view.findViewById<FlexboxLayout>(R.id.fl_skills)

        context?.let {
            prefManager = PrefManager(it)
            et_company_name?.setText(prefManager.profileCompanyName)
            Glide.with(it).load(prefManager.profileCompanyLogo).into(iv_logo)
            iv_logo.setOnClickListener {
                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(prefManager.profileLinkedinCompany))
                startActivity(intent)
            }
        }

        et_summary?.setOnTouchListener(OnTouchListener { v, event ->
            if (v.id == R.id.et_summary) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        })

        et_skills?.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                val txtVal = v.text
                if (!txtVal.isNullOrEmpty()) {
                    if (!skillsList.contains(txtVal.toString())) {
                        addNewChip(txtVal.toString(), fl_skills)
                        et_skills?.setText("")
                    } else {
                        Snackbar.make(view, "Skill already added", Snackbar.LENGTH_SHORT).show()
                    }
                }
                return@OnEditorActionListener true
            }
            false
        })

        card_post.setOnClickListener {
            if (et_job_title?.text.toString().isNotEmpty() and et_experience?.text.toString()
                    .isNotEmpty()
                and et_summary?.text.toString().isNotEmpty() and et_location?.text.toString()
                    .isNotEmpty()
            ) {
                val job = JobItemModel()
                job.job_title = et_job_title?.text.toString()
                job.experience = et_experience?.text.toString()
                job.summary = et_summary?.text.toString()
                job.location = et_location?.text.toString()
                job.skills = Gson().toJson(skillsList)
                job.company_name = prefManager.profileCompanyName
                job.company_logo = prefManager.profileCompanyLogo
                job.company_linkedin = prefManager.profileLinkedinCompany
                val database = FirebaseDatabase.getInstance()

                val mAuth = FirebaseAuth.getInstance()
                val currentUser = mAuth.currentUser

                val key = database.reference.child("jobs").push().key
                if (currentUser != null) {
                    if (key != null) {
                        job.job_id = key
                        job.job_poster = currentUser.uid
                        database.reference.child("jobs").child(key).setValue(job)
                        dismiss()
                        Snackbar.make(card_post, "Job posted!", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            else {
                Snackbar.make(card_post, "Incomplete details!", Snackbar.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun addNewChip(skill: String, chipGroup: FlexboxLayout) {
        if (!skillsList.contains(skill)) {

            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 10, 0)
            skillsList.add(skill)
            val chip = Chip(context)
            chip.text = skill
            chip.isCloseIconEnabled = true
            chip.isClickable = true
            chip.isCheckable = false
            chipGroup.addView(chip as View, chipGroup.childCount - 1, layoutParams)
            chip.setOnCloseIconClickListener {
                chipGroup.removeView(chip as View)
                skillsList.remove(skill)
            }
        }
    }

}