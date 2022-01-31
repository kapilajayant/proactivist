package com.jayant.proactivist.activities

import com.jayant.proactivist.R
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.jayant.proactivist.adapters.ApplicationAdapter
import com.jayant.proactivist.fragments.NoInternetFragment
import com.jayant.proactivist.models.Application
import com.jayant.proactivist.models.ListResponseModel
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.DialogHelper
import com.jayant.proactivist.utils.NetworkManager
import com.jayant.proactivist.utils.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ViewApplicationsActivity : AppCompatActivity() {

    lateinit var apiService: APIService
    private var selectedStatus = ""
    private lateinit var reference: Query
    private lateinit var listener: ValueEventListener
    private lateinit var rv_applications: RecyclerView
    private lateinit var tv_app_bar: TextView
    private lateinit var iv_back: ImageView
    private lateinit var chip_reject: Chip
    private lateinit var chip_pending: Chip
    private lateinit var chip_accept: Chip
    private lateinit var applicationAdapter: ApplicationAdapter
    private var applicationList = ArrayList<Application>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applications)

        apiService = ApiUtils.getAPIService()

        rv_applications = findViewById(R.id.rv_applications)
        tv_app_bar = findViewById(R.id.tv_app_bar)
        iv_back = findViewById(R.id.iv_back)
        chip_reject = findViewById(R.id.chip_reject)
        chip_pending = findViewById(R.id.chip_pending)
        chip_accept = findViewById(R.id.chip_accept)

        iv_back.setOnClickListener {
            finish()
        }

        getData()

        chip_reject.setOnClickListener {
            if(!chip_reject.isSelected){
                chip_reject.isSelected = true
                chip_accept.isSelected = false
                chip_pending.isSelected = false
                selectedStatus = Constants.REJECTED.toString()
                chip_accept.chipBackgroundColor = null
                chip_pending.chipBackgroundColor = null
                chip_reject.chipBackgroundColor = resources.getColorStateList(R.color.red_tint, null)
            }
            else{
                chip_reject.isSelected = false
                selectedStatus = ""
                chip_reject.chipBackgroundColor = null
            }
            getData()
        }

        chip_pending.setOnClickListener {
            if(!chip_pending.isSelected){
                chip_pending.isSelected = true
                chip_accept.isSelected = false
                chip_reject.isSelected = false
                selectedStatus = Constants.PENDING.toString()
                chip_accept.chipBackgroundColor = null
                chip_reject.chipBackgroundColor = null
                chip_pending.chipBackgroundColor = resources.getColorStateList(R.color.yellow_tint, null)
            }
            else{
                chip_pending.isSelected = false
                selectedStatus = ""
                chip_pending.chipBackgroundColor = null
            }
            getData()
        }

        chip_accept.setOnClickListener {
            if(!chip_accept.isSelected){
                chip_accept.isSelected = true
                chip_reject.isSelected = false
                chip_pending.isSelected = false
                selectedStatus = Constants.ACCEPTED.toString()
                chip_pending.chipBackgroundColor = null
                chip_reject.chipBackgroundColor = null
                chip_accept.chipBackgroundColor = resources.getColorStateList(R.color.green_tint, null)
            }
            else{
                chip_accept.isSelected = false
                selectedStatus = ""
                chip_accept.chipBackgroundColor = null
            }
            getData()
        }

        val prefManager = PrefManager(this)
        if (prefManager.profileRole == Constants.REFERRER) {
            tv_app_bar.text = "History"
            rv_applications.apply {
                applicationAdapter = ApplicationAdapter(this@ViewApplicationsActivity, applicationList, true, supportFragmentManager)
                adapter = applicationAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }
        }
        else {
            tv_app_bar.text = "Applications"
            rv_applications.apply {
                applicationAdapter = ApplicationAdapter(this@ViewApplicationsActivity, applicationList, supportFragmentManager)
                adapter = applicationAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }
        }
    }

    private fun getData() {
        applicationList.clear()
        val prefManager = PrefManager(this)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        prefManager.profileRole?.let {
            if (uid != null) {
                if(NetworkManager.getConnectivityStatusString(this@ViewApplicationsActivity) != Constants.NO_INTERNET) {
                    getApplications(uid, it)
                }
                else{
                    val fragment = NoInternetFragment()
                    fragment.show(supportFragmentManager, "")
                }
            }
        }
    }

    private fun getApplications(uid: String, user_type: String) {
        apiService.getApplications(uid, user_type + "s", selectedStatus).enqueue(object : Callback<ListResponseModel> {
            override fun onResponse(
                call: Call<ListResponseModel>,
                response: Response<ListResponseModel>
            ){
                DialogHelper.hideLoadingDialog()
                if (response.isSuccessful) {
                    try {
                        if(response.code() == 200) {
                            applicationList.clear()
                            response.body()?.getApplicationsResponse()?.let {
                                applicationList.addAll(it)
                                applicationAdapter.notifyDataSetChanged()
                            }
                        }

                        else {
                            Toast.makeText(
                                this@ViewApplicationsActivity,
                                response.body()?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            override fun onFailure(call: Call<ListResponseModel>, t: Throwable) {
                t.printStackTrace()
                DialogHelper.hideLoadingDialog()
            }
        })
    }

}