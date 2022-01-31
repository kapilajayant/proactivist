package com.jayant.proactivists.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.jayant.proactivists.R
import com.jayant.proactivists.models.ResponseModel
import com.jayant.proactivists.models.get_referrers.GetReferrersItem
import com.jayant.proactivists.rest.APIService
import com.jayant.proactivists.rest.ApiUtils
import com.jayant.proactivists.utils.Constants
import com.jayant.proactivists.utils.NetworkManager
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse

class ReferralProfileFragment(var photo: String, var referrer: GetReferrersItem?) : BottomSheetDialogFragment() {

    private var et_job_id: EditText? = null
    private var tv_profile_name: TextView? = null
    private var tv_company_name: TextView? = null
    private lateinit var tv_profile: TextView
    private lateinit var iv_profile: ImageView
    private lateinit var iv_logo: CircleImageView
    private lateinit var card_ask: CardView
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
        tv_profile_name = view.findViewById(R.id.tv_profile_name)
        et_job_id = view.findViewById(R.id.et_job_id)
        card_ask = view.findViewById(R.id.card_ask)
        linear_ask = view.findViewById(R.id.linear_ask)
        tv_profile = view.findViewById(R.id.tv_profile)

        tv_profile_name?.text = referrer?.company_name
//        tv_company_name?.text = referrer?.referrer_name

        Glide.with(requireContext()).load(referrer?.company_logo).into(iv_profile)
//        Glide.with(requireContext()).load(photo).into(iv_logo)

        card_ask.isEnabled = false

        et_job_id?.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if(p0?.isNotEmpty() == true){
                    linear_ask.background =
                        context?.let { ContextCompat.getColor(it, R.color.black) }?.let {
                            ColorDrawable(
                                it
                            )
                        }
                    card_ask.isEnabled = true
                }
                else{
                    linear_ask.background =
                        context?.let { ContextCompat.getColor(it, R.color.gray) }?.let {
                            ColorDrawable(
                                it
                            )
                        }
                    card_ask.isEnabled = false
                }
            }

        })
        card_ask.setOnClickListener {
            val mAuth = FirebaseAuth.getInstance()
            val currentUser = mAuth.currentUser
            val database = FirebaseDatabase.getInstance()
            referrer?.ref_gid?.let { referrerId -> currentUser?.uid?.let { candidateId ->

                if (NetworkManager.getConnectivityStatusString(requireContext()) != Constants.NO_INTERNET) {
                    sendRequest(candidateId)
                }
                else {
                    val fragment = NoInternetFragment()
                    fragment.show(parentFragmentManager, "")
                }

            } }
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
                            Toast.makeText(requireContext(), "Request sent!", Toast.LENGTH_SHORT).show()
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
            }

            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show()
                Log.d("profile_backend", "onFailure: ${call.request().url()}")
                t.printStackTrace()
            }

        })
    }


}