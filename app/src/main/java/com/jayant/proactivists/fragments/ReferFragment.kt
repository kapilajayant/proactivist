package com.jayant.proactivists.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jayant.proactivists.BuildConfig
import com.jayant.proactivists.R
import com.jayant.proactivists.activities.ProfileActivity
import com.jayant.proactivists.activities.SearchActivity
import com.jayant.proactivists.activities.ViewApplicationsActivity
import com.jayant.proactivists.adapters.ProfileAdapter
import com.jayant.proactivists.models.ListResponseModel
import com.jayant.proactivists.rest.APIService
import com.jayant.proactivists.rest.ApiUtils
import com.jayant.proactivists.utils.Constants
import com.jayant.proactivists.utils.DialogHelper
import com.jayant.proactivists.utils.NetworkManager
import com.jayant.proactivists.utils.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReferFragment : Fragment() {

    private var currentUser: FirebaseUser? = null
    lateinit var apiService: APIService
    private lateinit var linear_empty: LinearLayout
    private lateinit var rv_refer: RecyclerView
    private lateinit var profilesAdapter: ProfileAdapter

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    private lateinit var iv_profile: ImageView
    private lateinit var iv_applications: ImageView
    private lateinit var tv_profile_name: TextView
    private lateinit var tvProfile: TextView
    private lateinit var tvCount: TextView
    private lateinit var card_search: CardView
    private lateinit var swipe: SwipeRefreshLayout

    private var count: Long = 0
    private var uiType = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
        apiService = ApiUtils.getAPIService()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_refer, container, false)

        tv_profile_name = view.findViewById(R.id.tv_profile_name)
        tvProfile = view.findViewById(R.id.tvProfile)
        iv_profile = view.findViewById(R.id.iv_profile)
        iv_applications = view.findViewById(R.id.iv_applications)
        card_search = view.findViewById(R.id.card_search)
        swipe = view.findViewById(R.id.swipe)

        iv_applications.visibility = View.VISIBLE

        swipe.setOnRefreshListener {
            if(NetworkManager.getConnectivityStatusString(requireContext()) != Constants.NO_INTERNET) {
                swipe.isRefreshing = true
                getData()
                setupProfile()
            }
            else{
                DialogHelper.hideLoadingDialog()
                swipe.isRefreshing = false
                val fragment = NoInternetFragment()
                fragment.show(parentFragmentManager, "")
            }
        }

        val prefManager = context?.let { PrefManager(it) }

        if (prefManager?.profileRole == Constants.REFERRER) {
            card_search.visibility = View.GONE
        }

        setupProfile()

        iv_applications.setOnClickListener {
            context?.let {
                if (NetworkManager.getConnectivityStatusString(it) != Constants.NO_INTERNET) {
                    val intent = Intent(context, ViewApplicationsActivity::class.java)
                    startActivity(intent)
                } else {
                    val fragment = NoInternetFragment()
                    fragment.show(parentFragmentManager, "")
                }
            }
        }

        card_search.setOnClickListener {
            val intent = Intent(context, SearchActivity::class.java)
            startActivity(intent)
        }

        rv_refer = view.findViewById<RecyclerView>(R.id.rv_refer)
        tvCount = view.findViewById(R.id.tvCount)
        linear_empty = view.findViewById<LinearLayout>(R.id.linear_empty)

        return view
    }

    private fun getData() {
        context?.let {
            DialogHelper.showLoadingDialog(requireActivity())
            swipe.isRefreshing = false
            val prefManager = PrefManager(it)
            if (prefManager.profileRole == Constants.REFERRER) {
                getCandidates()
//                getCandidatesFirebase()
                uiType = Constants.SHOW_CANDIDATES_LIST
            } else {
                getReferrers()
//                getReferrersFirebase()
                uiType = Constants.SHOW_COMPANIES_LIST
            }
        }

        rv_refer.apply {
            profilesAdapter = ProfileAdapter(context, uiType, requireFragmentManager())
            adapter = profilesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun getCandidates() {
        currentUser?.uid?.let { uid ->
            apiService.getCandidates(uid).enqueue(object : Callback<ListResponseModel> {
                override fun onResponse(
                    call: Call<ListResponseModel>,
                    response: Response<ListResponseModel>
                ) {
                    DialogHelper.hideLoadingDialog()
                    if (response.isSuccessful) {
                        try {
                            if(response.code() == 200) {
                                response.body()?.getCandidatesResponse()?.let {
                                    if (it.size == 0) {
                                        rv_refer.visibility = View.GONE
                                        linear_empty.visibility = View.VISIBLE
                                    } else {
                                        linear_empty.visibility = View.GONE
                                        rv_refer.visibility = View.VISIBLE
                                        profilesAdapter.setCandidatesList(
                                            it
                                        )
                                    }
                                }
                            }
                            else {
                                Toast.makeText(
                                    requireContext(),
                                    response.body()?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            DialogHelper.hideLoadingDialog()
                            rv_refer.visibility = View.GONE
                            linear_empty.visibility = View.VISIBLE
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<ListResponseModel>, t: Throwable) {
                    DialogHelper.hideLoadingDialog()
                    rv_refer.visibility = View.GONE
                    linear_empty.visibility = View.VISIBLE
                    t.printStackTrace()
                }
            })
        }
    }

    private fun getReferrers() {

        currentUser?.uid?.let { uid ->
            apiService.getReferrers(uid).enqueue(object : Callback<ListResponseModel> {
                override fun onResponse(
                    call: Call<ListResponseModel>,
                    response: Response<ListResponseModel>
                ) {
                    DialogHelper.hideLoadingDialog()
                    if (response.isSuccessful) {
                        try {
                            if(response.code() == 200) {
                                response.body()?.getReferrersResponse()?.let {
                                    if (it.size == 0) {
                                        rv_refer.visibility = View.GONE
                                        linear_empty.visibility = View.VISIBLE
                                    } else {
                                        linear_empty.visibility = View.GONE
                                        rv_refer.visibility = View.VISIBLE
                                        profilesAdapter.setReferrersList(
                                            it
                                        )
                                    }
                                }
                            }
                            else {
                                Toast.makeText(
                                    requireContext(),
                                    response.body()?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            DialogHelper.hideLoadingDialog()
                            rv_refer.visibility = View.GONE
                            linear_empty.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onFailure(call: Call<ListResponseModel>, t: Throwable) {
                    t.printStackTrace()
                    DialogHelper.hideLoadingDialog()
                    rv_refer.visibility = View.GONE
                    linear_empty.visibility = View.VISIBLE
                }
            })
        }
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(param1: String?, param2: String?): ReferFragment {
            val fragment = ReferFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    private fun setupProfile() {
        context?.let { context ->
            currentUser = FirebaseAuth.getInstance().currentUser
            tv_profile_name.text = "Hi " + currentUser?.displayName

            Glide.with(context).load(currentUser?.photoUrl.toString())
                .apply(RequestOptions.circleCropTransform()).into(iv_profile)

            iv_profile.setOnClickListener {
                if(NetworkManager.getConnectivityStatusString(context) != Constants.NO_INTERNET) {
                    val intent = Intent(context, ProfileActivity::class.java)
                    context.startActivity(intent)
                }
                else {
                    val fragment = NoInternetFragment()
                    fragment.show(parentFragmentManager, "")
                }
            }
            tvProfile.setOnClickListener {
                val intent = Intent(context, ProfileActivity::class.java)
                context.startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(NetworkManager.getConnectivityStatusString(requireContext()) != Constants.NO_INTERNET) {
            getData()
        }
        else{
            DialogHelper.hideLoadingDialog()
            swipe.isRefreshing = false
            val fragment = NoInternetFragment()
            fragment.show(parentFragmentManager, "")
        }
    }

}