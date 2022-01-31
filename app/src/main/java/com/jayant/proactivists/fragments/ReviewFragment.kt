package com.jayant.proactivists.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.jayant.proactivists.R
import com.jayant.proactivists.rest.APIService
import com.jayant.proactivists.rest.ApiUtils
import com.jayant.proactivists.utils.Constants
import com.jayant.proactivists.utils.Constants.FEEDBACK
import com.jayant.proactivists.utils.Constants.HELP_COINS
import com.jayant.proactivists.utils.Constants.HELP_HOW_IT_WORKS
import com.jayant.proactivists.utils.Constants.HELP_MY_ACCOUNT
import com.jayant.proactivists.utils.Constants.HELP_STATUS
import com.jayant.proactivists.utils.Constants.ISSUE
import com.jayant.proactivists.utils.Constants.QUERY

class ReviewFragment : BottomSheetDialogFragment() {

    private lateinit var et_message: EditText
    private lateinit var card_send: CardView
    private lateinit var tv_status: TextView
    lateinit var apiService: APIService
    private lateinit var chip_feedback: Chip
    private lateinit var chip_query: Chip
    private lateinit var chip_issue: Chip
    private var selectedStatus = FEEDBACK
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        apiService = ApiUtils.getAPIService()

        val view = inflater.inflate(R.layout.layout_review, container, false)

        chip_feedback = view.findViewById(R.id.chip_feedback)
        chip_query = view.findViewById(R.id.chip_query)
        chip_issue = view.findViewById(R.id.chip_issue)
        et_message = view.findViewById(R.id.et_message)
        card_send = view.findViewById(R.id.card_send)
        tv_status = view.findViewById(R.id.tv_status)

        chip_feedback.isSelected = true
        chip_feedback.chipBackgroundColor = resources.getColorStateList(R.color.green_tint, null)

        chip_feedback.setOnClickListener {
            if(!chip_feedback.isSelected){
                tv_status.text = "I want to give some feedback."
                chip_feedback.isSelected = true
                chip_issue.isSelected = false
                chip_query.isSelected = false
                selectedStatus = FEEDBACK
                chip_query.chipBackgroundColor = null
                chip_issue.chipBackgroundColor = null
                chip_feedback.chipBackgroundColor = resources.getColorStateList(R.color.green_tint, null)
            }
        }

        chip_issue.setOnClickListener {
            if(!chip_issue.isSelected){
                tv_status.text = "I want to report an issue."
                chip_issue.isSelected = true
                chip_feedback.isSelected = false
                chip_query.isSelected = false
                selectedStatus = ISSUE
                chip_feedback.chipBackgroundColor = null
                chip_query.chipBackgroundColor = null
                chip_issue.chipBackgroundColor = resources.getColorStateList(R.color.red_tint, null)
            }
            else{
                tv_status.text = "I want to give some feedback."
                chip_issue.isSelected = false
                selectedStatus = FEEDBACK
                chip_issue.chipBackgroundColor = null
                chip_feedback.chipBackgroundColor = resources.getColorStateList(R.color.green_tint, null)
            }
        }

        chip_query.setOnClickListener {
            if(!chip_query.isSelected){
                tv_status.text = "I have some query."
                chip_query.isSelected = true
                chip_feedback.isSelected = false
                chip_issue.isSelected = false
                selectedStatus = QUERY
                chip_feedback.chipBackgroundColor = null
                chip_issue.chipBackgroundColor = null
                chip_query.chipBackgroundColor = resources.getColorStateList(R.color.yellow_tint, null)
            }
            else{
                tv_status.text = "I want to give some feedback."
                chip_query.isSelected = false
                selectedStatus = FEEDBACK
                chip_query.chipBackgroundColor = null
                chip_feedback.chipBackgroundColor = resources.getColorStateList(R.color.green_tint, null)
            }
        }

        card_send.setOnClickListener {
            sendMessage(selectedStatus, et_message.text.toString())
        }


        return view
    }

    private fun sendMessage(selectedStatus: Int, message: String) {
        
    }

}