package com.jayant.proactivist.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.jayant.proactivist.R
import com.jayant.proactivist.activities.ProfileActivity
import com.jayant.proactivist.adapters.TopicAdapter
import com.jayant.proactivist.models.ListResponseModel
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.models.learn.TopicModel
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.utils.DialogHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class LearnFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    lateinit var apiService: APIService
    private lateinit var rv_topics : RecyclerView
    private lateinit var topicAdapter: TopicAdapter
    private var topicsList = ArrayList<TopicModel>()
    private lateinit var iv_profile : ImageView
    private lateinit var tv_profile_name : TextView
    private lateinit var fab_post: FloatingActionButton

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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_learn, container, false)

        rv_topics = view.findViewById(R.id.rv_topics)
        fab_post = view.findViewById(R.id.fab_post)
        tv_profile_name = view.findViewById(R.id.tv_profile_name)
        iv_profile = view.findViewById(R.id.iv_profile)

//        topicsList.add(TopicModel("Android", "https://firebasestorage.googleapis.com/v0/b/proactivists-ba8ef.appspot.com/o/assets%2Fandroid.png?alt=media&token=cf78ced7-47fe-4786-814d-b5a0d830e599"))
//        topicsList.add(TopicModel("Python", "https://firebasestorage.googleapis.com/v0/b/proactivists-ba8ef.appspot.com/o/assets%2Fic_python.png?alt=media&token=5f5b4c78-2b8f-443e-b782-940e52bc9be7"))
//        topicsList.add(TopicModel("React", "https://firebasestorage.googleapis.com/v0/b/proactivists-ba8ef.appspot.com/o/assets%2Fic_react.png?alt=media&token=708a31c1-5a91-41a5-a697-68c53147f4ad"))
//        topicsList.add(TopicModel("Django", "https://firebasestorage.googleapis.com/v0/b/proactivists-ba8ef.appspot.com/o/assets%2Fic_django.png?alt=media&token=b6da066f-66ab-4ecc-a04a-481bc678fb91"))
//        topicsList.add(TopicModel("iOS", "https://firebasestorage.googleapis.com/v0/b/proactivists-ba8ef.appspot.com/o/assets%2Fic_ios.png?alt=media&token=c9307f5e-3e3f-44ee-83d6-f28d4e08f76e"))
//        topicsList.add(TopicModel("SQL", "https://firebasestorage.googleapis.com/v0/b/proactivists-ba8ef.appspot.com/o/assets%2Fsql.png?alt=media&token=7033bf6e-62fa-477d-bf77-1bb8b27f2427"))
//        topicsList.add(TopicModel("Devops", "https://www.docker.com/sites/default/files/d8/2019-07/horizontal-logo-monochromatic-white.png"))
//        topicsList.add(TopicModel("NodeJs", "https://firebasestorage.googleapis.com/v0/b/proactivists-ba8ef.appspot.com/o/assets%2Fnode%20js.png?alt=media&token=9a66df59-b5c3-4efe-9521-e5f866d7eac3"))

        topicAdapter = TopicAdapter(requireContext(), topicsList, parentFragmentManager)

        rv_topics.apply {
            layoutManager = GridLayoutManager(requireContext(), 2, LinearLayoutManager.VERTICAL, false)
            adapter = topicAdapter
        }

        getTopics()
        setupProfile()

        fab_post.setOnClickListener {
            val newTopicPostFragment = NewTopicPostFragment()
            fragmentManager?.let { it1 -> newTopicPostFragment.show(it1, "") }
        }

        return view
    }

    private fun getTopics() {

        DialogHelper.showLoadingDialog(requireActivity())

        apiService.getTopics().enqueue(object : Callback<ListResponseModel> {
            override fun onResponse(
                call: Call<ListResponseModel>,
                response: Response<ListResponseModel>
            ) {
                DialogHelper.hideLoadingDialog()
                if (response.isSuccessful) {
                    try {
                        if(response.code() == 200){
                            response.body()?.getTopicsResponse()?.forEach {
                                topicsList.add(it)
                            }
                            topicAdapter.notifyDataSetChanged()
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

            override fun onFailure(call: Call<ListResponseModel>, t: Throwable) {
                DialogHelper.hideLoadingDialog()
                Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }

        })
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(param1: String?, param2: String?): LearnFragment {
            val fragment = LearnFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    private fun setupProfile(){
        val currentUser = FirebaseAuth.getInstance().currentUser
//        tv_profile_name.text = "Hi " + currentUser?.displayName?.substringBefore(" ")
        tv_profile_name.text = "Learn"
        context?.let { context ->
            Glide.with(context).load(currentUser?.photoUrl).apply(RequestOptions.circleCropTransform()).into(iv_profile)
            iv_profile.setOnClickListener {
                val intent = Intent(context, ProfileActivity::class.java)
                context.startActivity(intent)
            }
        }
    }
}