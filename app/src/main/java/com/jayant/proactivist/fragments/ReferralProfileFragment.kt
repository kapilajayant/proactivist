package com.jayant.proactivist.fragments

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.jayant.proactivist.R
import com.jayant.proactivist.activities.ReferrerProfileActivity
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.models.get_referrers.GetReferrersItem
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.NetworkManager
import com.jayant.proactivist.utils.NotificationManager
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse

class ReferralProfileFragment(var referrer: GetReferrersItem, val referCallback: ReferCallback) : BottomSheetDialogFragment() {

    private var et_job_id: EditText? = null
    private var tv_profile_name: TextView? = null
    private var tv_company_name: TextView? = null
    private var tv_info: TextView? = null
    private lateinit var tv_profile: TextView
    private lateinit var iv_profile: CircleImageView
    private lateinit var iv_logo: CircleImageView
    private lateinit var card_ask: Button
    private lateinit var linear_ask: LinearLayout
    lateinit var apiService: APIService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        apiService = ApiUtils.getAPIService()

        val view = inflater.inflate(R.layout.layout_referrer_profile, container, false)

        val rootView: View? = activity?.window?.decorView?.findViewById(android.R.id.content)

        iv_profile = view.findViewById(R.id.iv_profile)
        iv_logo = view.findViewById(R.id.iv_logo)
        tv_company_name = view.findViewById(R.id.tv_company_name)
        tv_info = view.findViewById(R.id.tv_info)
        tv_profile_name = view.findViewById(R.id.tv_profile_name)
        et_job_id = view.findViewById(R.id.et_job_id)
        card_ask = view.findViewById(R.id.card_ask)
        tv_profile = view.findViewById(R.id.tv_profile)

        tv_profile_name?.text = referrer?.company_name
//        tv_company_name?.text = referrer?.referrer_name

        iv_profile.setOnClickListener {
            val intent = Intent(requireContext(), ReferrerProfileActivity::class.java)
            val ref = referrer
            intent.putExtra("ref_gid", ref.ref_gid)
            startActivity(intent)
        }

        Glide.with(requireContext()).load(referrer?.company_logo).into(iv_profile)
//        Glide.with(requireContext()).load(photo).into(iv_logo)

        val text = "Your profile will be shared with "
        val ref = referrer?.referrer_name?.substringBefore(" ")
        card_ask.isEnabled = false
        val spannable = SpannableString(text + ref)
        ref?.length?.let {
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.purple_500)),
                text.length,
                text.length + it,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                text.length,
                text.length + it,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                RelativeSizeSpan(1.5f),
                text.length,
                text.length + it,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                UnderlineSpan(),
                text.length,
                text.length + it,
                0)
        }

        tv_info?.text = spannable
        tv_info?.setOnClickListener {
            val intent = Intent(requireContext(), ReferrerProfileActivity::class.java)
            val ref = referrer
            intent.putExtra("ref_gid", ref.ref_gid)
            startActivity(intent)
        }

        et_job_id?.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if(p0?.isNotEmpty() == true){
//                    linear_ask.background =
//                        context?.let { ContextCompat.getColor(it, R.color.black) }?.let {
//                            ColorDrawable(
//                                it
//                            )
//                        }
                    card_ask.isEnabled = true
                }
                else{
//                    linear_ask.background =
//                        context?.let { ContextCompat.getColor(it, R.color.gray) }?.let {
//                            ColorDrawable(
//                                it
//                            )
//                        }
//                    card_ask.isEnabled = false
                }
            }

        })
        card_ask.setOnClickListener {
            if(et_job_id?.text.toString().contains("http")) {
                val mAuth = FirebaseAuth.getInstance()
                val currentUser = mAuth.currentUser
                val database = FirebaseDatabase.getInstance()
                referrer?.ref_gid?.let { referrerId ->
                    currentUser?.uid?.let { candidateId ->

                        if (NetworkManager.getConnectivityStatusString(requireContext()) != Constants.NO_INTERNET) {
                            sendRequest(candidateId)
                        } else {
                            val fragment = NoInternetFragment()
                            fragment.show(parentFragmentManager, "")
                        }

                    }
                }
            }
            else{
                Toast.makeText(requireContext(), "Invalid job link!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun sendRequest(candidateId: String) {

        val jsonObject = JSONObject()
        jsonObject.put("job_id", et_job_id?.text.toString())
        jsonObject.put("can_gid", candidateId)
        jsonObject.put("ref_gid", referrer?.ref_gid)
        val body = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            jsonObject.toString()
        )
        apiService.createApplication(body).enqueue(object : Callback<ResponseModel> {
            override fun onResponse(
                call: Call<ResponseModel>,
                response: Response<ResponseModel>
            ) {
                if (response.isSuccessful) {
                    try {
                        if(response.code() == 200){
                            referrer?.ref_gid?.let {
                                referrer?.token?.let { it1 ->
                                    NotificationManager.sendNotification(token = it1, Constants.PENDING, candidateId,
                                        it
                                    )
                                }
                            }
                            Toast.makeText(requireContext(), "Request sent!", Toast.LENGTH_SHORT).show()
                            dismiss()
                            referCallback.closeReferrer()
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
interface ReferCallback{
    fun closeReferrer()
}