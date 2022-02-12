package com.jayant.proactivist.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jayant.proactivist.R
import com.jayant.proactivist.adapters.CompanySearchAdapter
import com.jayant.proactivist.adapters.CompanySelected
import com.jayant.proactivist.adapters.ProfileAdapter
import com.jayant.proactivist.fragments.ReferCallback
import com.jayant.proactivist.models.CompanySuggestion
import com.jayant.proactivist.models.ListResponseModel
import com.jayant.proactivist.models.Profile
import com.jayant.proactivist.models.get_candidates.GetCandidatesItem
import com.jayant.proactivist.models.get_referrers.GetReferrersItem
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.rest.CompanySearchAPIService
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.Constants.SHOW_COMPANIES_LIST
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity(), ReferCallback {

    private lateinit var progress: LinearProgressIndicator
    private val TAG = SearchActivity::class.java.simpleName
    private lateinit var et_search: AutoCompleteTextView
    private lateinit var rv_profiles: RecyclerView
    private lateinit var linear_empty: LinearLayout
    private lateinit var iv_back: ImageView
    lateinit var apiService: APIService
    private var suggestions = ArrayList<CompanySuggestion>()
    private var referrers = ArrayList<GetReferrersItem>()
    private var profileList = ArrayList<Profile>()
    private lateinit var profileAdapter: ProfileAdapter
    private var count: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        apiService = ApiUtils.getAPIService()

        progress = findViewById(R.id.progress)
        et_search = findViewById(R.id.et_search)
        rv_profiles = findViewById(R.id.rv_profiles)
        linear_empty = findViewById(R.id.linear_empty)
        iv_back = findViewById(R.id.iv_back)

        iv_back.setOnClickListener {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(
                window.decorView.windowToken,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
            finish()
        }

        et_search.showSoftInputOnFocus = true
        et_search.requestFocus()
        et_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(p0.toString().length >= 3){
                    progress.show()
                    searchForCompany(p0.toString())
                }
                else{
                    if(!referrers.isNullOrEmpty()){
                        referrers.clear()
                        profileAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
        )

        rv_profiles.apply {
            profileAdapter = ProfileAdapter(context, Constants.SHOW_COMPANIES_LIST, supportFragmentManager)
            adapter = profileAdapter
            profileAdapter.setReferrerCallback(this@SearchActivity)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun searchForCompany(query: String) {

        referrers.clear()
        profileAdapter.notifyDataSetChanged()
        apiService.search(query).enqueue(object : Callback<ListResponseModel> {
            override fun onResponse(
                call: Call<ListResponseModel>,
                response: Response<ListResponseModel>
            ) {
                try {
                    progress.visibility = View.GONE
//                    val candidatesItem = ArrayList<GetCandidatesItem>()
                    Log.d(TAG, "onResponse: " + response.body())
                    val results = response.body()?.getReferrersResponse()
                    if(results.isNullOrEmpty()){
                        linear_empty.visibility = View.VISIBLE
                        rv_profiles.visibility = View.GONE
                    }
                    else {
                        linear_empty.visibility = View.GONE
                        rv_profiles.visibility = View.VISIBLE
                        results.forEach {
                            if (!referrers.contains(it)) {
                                referrers.add(it)
                            }
//                        candidatesItem.add(GetCandidatesItem("", "", it.ref_gid, it.referrer_name, it.company_logo, 192, "", it.company_linkedin, it.company_logo, it.company_name))
                        }
                        profileAdapter.setReferrersList(referrers)
                        profileAdapter.notifyDataSetChanged()
                    }
                } catch (e: Exception) {
                    progress.visibility = View.GONE
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ListResponseModel>, t: Throwable) {
                progress.visibility = View.GONE
                t.printStackTrace()
            }
        })
    }

    override fun closeReferrer() {

    }

}