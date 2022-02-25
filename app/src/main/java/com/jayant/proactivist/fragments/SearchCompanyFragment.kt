package com.jayant.proactivist.fragments

import com.jayant.proactivist.R
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jayant.proactivist.adapters.CompanyAdapter
import com.jayant.proactivist.adapters.CompanySelected
import com.jayant.proactivist.models.CompanySuggestion
import com.jayant.proactivist.models.ListResponseModel
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.rest.CompanySearchAPIService
import com.jayant.proactivist.utils.DialogHelper
import com.jayant.proactivist.utils.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchCompanyFragment(val companySelected: CompanySelected) : BottomSheetDialogFragment() {

    private lateinit var progress: LinearProgressIndicator
    private lateinit var iv_search: ImageView
    private lateinit var rv_companies: RecyclerView
    private var etCompany: EditText? = null
    lateinit var companySearchApiService: CompanySearchAPIService
    private var suggestions = ArrayList<CompanySuggestion>()
    private lateinit var companyAdapter: CompanyAdapter
    private var runnable: Runnable? = null
    private var handler: Handler? = null
    private var timeToWait = 500L //change this one for delay (time in milli)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.layout_search_company, container, false)

        companySearchApiService = ApiUtils.getCompanySearchAPIService()

        progress = view.findViewById(R.id.progress)
        iv_search = view.findViewById(R.id.iv_search)
        etCompany = view.findViewById(R.id.et_company)
        rv_companies = view.findViewById(R.id.rv_companies)

        etCompany?.requestFocus()
        etCompany?.showSoftInputOnFocus = true

        iv_search.setOnClickListener {
            if (etCompany?.text.toString().isEmpty().not()) {
                progress.show()
                searchForCompany(etCompany?.text.toString())
            }
        }

        etCompany?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (etCompany?.text.toString().isEmpty().not()) {
                    progress.show()
                    searchForCompany(etCompany?.text.toString())
                }
            }
        }
        )

        companyAdapter = CompanyAdapter(requireContext(), suggestions, companySelected)

        rv_companies.apply {
            adapter = companyAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }

        return view
    }

    private fun searchForCompany(query: String) {
        suggestions.clear()
        companySearchApiService.search(query)
            .enqueue(object : Callback<ArrayList<CompanySuggestion>> {
                override fun onResponse(
                    call: Call<ArrayList<CompanySuggestion>>,
                    response: Response<ArrayList<CompanySuggestion>>
                ) {
                    try {
                        progress.visibility = View.GONE
                        Log.d("TAG", "onResponse: " + response.body())
                        response.body()?.forEach {
                            suggestions.add(it)
                        }
                        try {
                            companyAdapter.notifyDataSetChanged()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        progress.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<ArrayList<CompanySuggestion>>, t: Throwable) {
                    t.printStackTrace()
                    progress.visibility = View.GONE
                }
            })
    }

}

