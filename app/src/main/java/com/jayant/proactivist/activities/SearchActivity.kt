package com.jayant.proactivist.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jayant.proactivist.R
import com.jayant.proactivist.adapters.CompanySearchAdapter
import com.jayant.proactivist.adapters.CompanySelected
import com.jayant.proactivist.adapters.ProfileAdapter
import com.jayant.proactivist.models.CompanySuggestion
import com.jayant.proactivist.models.Profile
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.rest.CompanySearchAPIService
import com.jayant.proactivist.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity(), CompanySelected {

    private val TAG = SearchActivity::class.java.simpleName
    private lateinit var et_search: AutoCompleteTextView
    private lateinit var rv_profiles: RecyclerView
    private lateinit var iv_back: ImageView
    lateinit var apiService: CompanySearchAPIService
    private var suggestions = ArrayList<CompanySuggestion>()
    private var profileList = ArrayList<Profile>()
    private lateinit var applicantsAdapter: ProfileAdapter
    private var count: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        apiService = ApiUtils.getCompanySearchAPIService()

        et_search = findViewById(R.id.et_search)
        rv_profiles = findViewById(R.id.rv_profiles)
        iv_back = findViewById(R.id.iv_back)

        iv_back.setOnClickListener {
            finish()
        }

        et_search.requestFocus()
        et_search.threshold = 3

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )

        et_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                searchForCompany(p0.toString())
            }

        }
        )

        rv_profiles.apply {
            applicantsAdapter = ProfileAdapter(context, Constants.SHOW_COMPANIES_LIST, supportFragmentManager)
            adapter = applicantsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun searchForCompany(query: String) {

        profileList.clear()
        applicantsAdapter.notifyDataSetChanged()

        apiService.search(query).enqueue(object : Callback<ArrayList<CompanySuggestion>> {
            override fun onResponse(
                call: Call<ArrayList<CompanySuggestion>>,
                response: Response<ArrayList<CompanySuggestion>>
            ) {
                try {
                    Log.d(TAG, "onResponse: " + response.body())
                    response.body()?.forEach {
                        suggestions.add(it)
                    }
                    val companySearchAdapter = CompanySearchAdapter(
                        this@SearchActivity,
                        R.layout.card_layout_job,
                        suggestions, this@SearchActivity
                    )
                    et_search.setAdapter(companySearchAdapter)

                    companySearchAdapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ArrayList<CompanySuggestion>>, t: Throwable) {
                t.printStackTrace()
            }

        })

    }

    override fun companySelected(companySuggestion: CompanySuggestion?) {

        profileList.clear()
        applicantsAdapter.notifyDataSetChanged()
        if (companySuggestion != null) {
            et_search.setText(companySuggestion.name)
            et_search.dismissDropDown()

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(et_search.windowToken, 0)

            searchProfiles(companySuggestion.name)
        }
    }

    private fun searchProfiles(name: String) {
        FirebaseDatabase.getInstance().reference.child("companies").child(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    snapshot.children.forEach {
                        val id = it.getValue(String::class.java)
                        getProfileById(id)
                    }

                    count = snapshot.childrenCount
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun getProfileById(id: String?){
        val database = FirebaseDatabase.getInstance()
        if (id != null) {
            database.reference.child("users").child(id).child("profile")
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val profile = snapshot.getValue(Profile::class.java)
                        profile?.uid = id
                        if (profile != null) {
                            profileList.add(profile)
                        }
                        if(profileList.size.toLong() == count){
                            applicantsAdapter.notifyDataSetChanged()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }
    }
}