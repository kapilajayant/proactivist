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
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.jayant.proactivist.activities.HelpActivity
import com.jayant.proactivist.R
import com.jayant.proactivist.activities.*
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.NetworkManager
import com.jayant.proactivist.utils.PrefManager
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class SettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    lateinit var apiService: APIService
    private lateinit var iv_profile: ImageView
    private lateinit var tv_profile_name: TextView
    private lateinit var tv_role: TextView
    private lateinit var tv_review: CardView
    private lateinit var tv_about_us: CardView
    private lateinit var card_chat: CardView
    private lateinit var card_coins: CardView
    private lateinit var card_help: CardView
    private lateinit var card_invite: CardView
    private var inviteCode = ""

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
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        tv_profile_name = view.findViewById(R.id.tv_profile_name)
        tv_role = view.findViewById(R.id.tv_role)
        iv_profile = view.findViewById(R.id.iv_profile)
        card_chat = view.findViewById(R.id.card_chat)
        card_coins = view.findViewById(R.id.card_coins)
        card_help = view.findViewById(R.id.card_help)
        card_invite = view.findViewById(R.id.card_invite)
        tv_review = view.findViewById(R.id.tv_review)
        tv_about_us = view.findViewById(R.id.tv_about_us)

        getCoins()
        setupProfile()

        val btn_sign_out: TextView = view.findViewById(R.id.btn_sign_out)
        val card_profile: CardView = view.findViewById(R.id.card_profile)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        val mAuth = FirebaseAuth.getInstance()

        btn_sign_out.setOnClickListener {
            mGoogleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mAuth.signOut()
                    val intent = Intent(context, WelcomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "Sign out failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        card_profile.setOnClickListener {
            if (NetworkManager.getConnectivityStatusString(requireContext()) != Constants.NO_INTERNET) {
                val intent = Intent(context, ProfileActivity::class.java)
                context?.startActivity(intent)
            }
            else {
                val fragment = NoInternetFragment()
                fragment.show(parentFragmentManager, "")
            }
        }
        tv_review.setOnClickListener {
            val fragment = ReviewFragment()
            fragment.show(parentFragmentManager, "")
        }
        tv_about_us.setOnClickListener {
            val intent = Intent(context, AboutUsActivity::class.java)
            startActivity(intent)
        }
        card_coins.setOnClickListener {
            val intent = Intent(context, CoinsActivity::class.java)
            startActivity(intent)
        }
        card_help.setOnClickListener {
            val intent = Intent(context, HelpActivity::class.java)
            startActivity(intent)
        }
        card_invite.setOnClickListener {
            var message = "I'm inviting you to join Proactivist. \nGet 50 coins by using my invite code: \n\n\nhttps://proactivist.in?inviteCode=$inviteCode \n\n\n Download the app now https://play.google.com/store/apps/details?id=com.jayant.proactivist"
            shareOthers(message)
        }
        card_chat.setOnClickListener {
            if (NetworkManager.getConnectivityStatusString(requireContext()) != Constants.NO_INTERNET) {
                val intent = Intent(context, ChatListActivity::class.java)
                startActivity(intent)
            } else {
                val fragment = NoInternetFragment()
                fragment.show(parentFragmentManager, "")
            }
        }
        return view
    }

    private fun setupProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        tv_profile_name.text = currentUser?.displayName
        context?.let { context ->
            Glide.with(context).load(currentUser?.photoUrl)
                .apply(RequestOptions.circleCropTransform()).into(iv_profile)
        }

        val prefManager = PrefManager(requireContext())
        inviteCode = ""

        val role = prefManager.profileRole
        if(role == Constants.CANDIDATE){
            tv_role.text = "Candidate"
        }
        else{
            tv_role.text = "Referrer"
        }
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(param1: String?, param2: String?): SettingsFragment {
            val fragment = SettingsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    private fun shareOthers(message: String){
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            message
        )
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, "choose one"))
    }

    private fun getCoins() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            apiService.getCoins(uid).enqueue(object : Callback<ResponseModel> {
                override fun onResponse(
                    call: Call<ResponseModel>,
                    response: Response<ResponseModel>
                ) {
                    if (response.isSuccessful) {
                        try {
                            if(response.code() == 200){
                                response.body()?.getCoinsResponse()?.invite_code.let {
                                    inviteCode = it.toString()
                                }
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
}