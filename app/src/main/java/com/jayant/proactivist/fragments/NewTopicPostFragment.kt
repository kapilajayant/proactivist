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


class NewTopicPostFragment: BottomSheetDialogFragment() {

    private lateinit var prefManager: PrefManager
    private var skillsList = arrayListOf<String>()
    private var et_skills: EditText? = null
    private var et_job_title: EditText? = null
    private var et_company_name: EditText? = null
    private var et_experience: EditText? = null
    private var et_summary: EditText? = null
    private var et_location: EditText? = null
    private var card_post: CardView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.layout_new_topic_post, container, false)



        context?.let {

        }

        return view
    }


}