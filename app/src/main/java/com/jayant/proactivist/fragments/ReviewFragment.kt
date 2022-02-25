package com.jayant.proactivist.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.jayant.proactivist.R
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.Constants.FEEDBACK
import com.jayant.proactivist.utils.Constants.ISSUE
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ReviewFragment : BottomSheetDialogFragment() {

    private lateinit var et_message: EditText
    private lateinit var card_send: Button
    private lateinit var tv_status: TextView
    lateinit var apiService: APIService
    private lateinit var chip_feedback: Chip
    private lateinit var chip_issue: Chip
    private var selectedStatus = FEEDBACK
    val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        apiService = ApiUtils.getAPIService()

        val view = inflater.inflate(R.layout.layout_review, container, false)

        chip_feedback = view.findViewById(R.id.chip_feedback)
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
                selectedStatus = FEEDBACK
                chip_issue.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                chip_issue.chipBackgroundColor = null
                chip_feedback.chipBackgroundColor = resources.getColorStateList(R.color.green_tint, null)
            }
        }

        chip_issue.setOnClickListener {
            if(!chip_issue.isSelected){
                chip_issue.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tv_status.text = "I want to report an issue."
                chip_issue.isSelected = true
                chip_feedback.isSelected = false
                selectedStatus = ISSUE
                chip_feedback.chipBackgroundColor = null
                chip_issue.chipBackgroundColor = resources.getColorStateList(R.color.red_tint, null)
            }
        }

        card_send.setOnClickListener {
            sendMessage(selectedStatus, et_message.text.toString())
        }

        return view
    }

    private fun sendMessage(selectedStatus: Int, message: String) {

        val timestamp = formatter.format(Date().time)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val jsonObject = JSONObject()
        jsonObject.put("feedback_type", selectedStatus)
        jsonObject.put("uid", uid)
        jsonObject.put("timestamp", timestamp)
        jsonObject.put("message", message)

        val body = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            jsonObject.toString()
        )
        if (uid != null) {
            apiService.feedback(body).enqueue(object : Callback<ResponseModel> {
                override fun onResponse(
                    call: Call<ResponseModel>,
                    response: Response<ResponseModel>
                ) {
                    if (response.isSuccessful) {
                        try {
                            if(response.code() == 200){
                                when(selectedStatus){
                                    FEEDBACK ->{
                                        Toast.makeText(requireContext(), "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                                    }
                                    ISSUE ->{
                                        Toast.makeText(requireContext(), "Your query has been submitted!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                dismiss()
                            }
                            else {
                                Toast.makeText(requireContext(), response.body()?.message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    }
                    else{
                        try {
                            val errorResponse = Gson().fromJson(response.errorBody()?.charStream(), ResponseModel::class.java)
                            Toast.makeText(requireContext(), errorResponse.message, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                    Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show()
                    Log.d("profile_backend", "onFailure: ${call.request().url()}")
                    t.printStackTrace()
                }
            })
        }
    }
}