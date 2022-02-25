package com.jayant.proactivist.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.jayant.proactivist.R
import com.jayant.proactivist.activities.EditProfileActivity
import com.jayant.proactivist.activities.HomeActivity
import com.jayant.proactivist.activities.UserRoleActivity
import com.jayant.proactivist.activities.WelcomeActivity
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.DialogHelper
import com.jayant.proactivist.utils.PrefManager
import com.jayant.proactivist.utils.ShortcutManagerUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectUserRoleFragment(var role: String, val allow: Boolean = false) :
    BottomSheetDialogFragment() {

    private lateinit var card_referrer: CardView
    private lateinit var card_candidate: CardView
    private lateinit var card_create_referrer: CardView
    private lateinit var card_create_candidate: CardView
    private lateinit var tv_select: TextView
    private lateinit var tv_sign_out: TextView
    lateinit var apiService: APIService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        isCancelable = allow
        apiService = ApiUtils.getAPIService()
        val view = inflater.inflate(R.layout.fragment_select_user_role, container, false)
        card_referrer = view.findViewById(R.id.card_referrer)
        card_candidate = view.findViewById(R.id.card_candidate)
        card_create_referrer = view.findViewById(R.id.card_create_referrer)
        card_create_candidate = view.findViewById(R.id.card_create_candidate)
        tv_select = view.findViewById(R.id.tv_select)
        tv_sign_out = view.findViewById<TextView>(R.id.tv_sign_out)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid
        if (uid != null) {
            checkUser(uid)
        }

        card_create_candidate.setOnClickListener {
            getProfile(Constants.REFERRER)
        }
        card_create_referrer.setOnClickListener {
            getProfile(Constants.CANDIDATE)
        }
        card_candidate.setOnClickListener {
            val prefManager = PrefManager(requireContext())
            prefManager.profileRole = Constants.CANDIDATE
            val i = Intent(requireContext(), HomeActivity::class.java)
            startActivity(i)
            activity?.finish()
        }
        card_referrer.setOnClickListener {
            val prefManager = PrefManager(requireContext())
            prefManager.profileRole = Constants.REFERRER
            val i = Intent(requireContext(), HomeActivity::class.java)
            startActivity(i)
            activity?.finish()
        }

        tv_sign_out.setOnClickListener {
            val mAuth = FirebaseAuth.getInstance()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

            mGoogleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ShortcutManagerUtil.removeShortcuts(requireContext())
                    mAuth.signOut()
                    PrefManager(requireContext()).clear()
                    val intent = Intent(requireContext(), WelcomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Sign out failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return view
    }

    private fun getProfile(profileRole: String) {
        DialogHelper.showLoadingDialog(requireActivity())
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid
        if (uid != null) {
            apiService.getProfile(uid, profileRole + "s")
                .enqueue(object : Callback<ResponseModel> {
                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {
                        if (response.isSuccessful) {
                            try {
                                if (response.code() == 200) {

                                    DialogHelper.hideLoadingDialog()
                                    response.body()?.getProfileResponse()?.let {
                                        val prefManager = PrefManager(requireContext())
                                        prefManager.setProfile(it)
                                        val i = Intent(
                                            requireContext(),
                                            EditProfileActivity::class.java
                                        )
                                        i.putExtra("role", profileRole)
                                        i.putExtra("profile", it)
                                        i.putExtra("switch", true)
                                        startActivity(i)
                                    }
                                } else {
                                    DialogHelper.hideLoadingDialog()
                                    Toast.makeText(
                                        requireContext(),
                                        response.body()?.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                DialogHelper.hideLoadingDialog()
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
                        DialogHelper.hideLoadingDialog()
                        Log.d("profile_backend", "onFailure: ${call.request().url()}")
                        t.printStackTrace()
                    }
                })
        }
    }


    private fun checkUser(uid: String) {
        apiService.checkUser(uid).enqueue(object : Callback<ResponseModel> {
            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                if (response.isSuccessful) {
                    try {
                        if (response.code() == 200) {
                            val data = response.body()?.getCheckUserResponse()
                            if (data?.is_exist == false) {
                            } else {
                                data?.user_type?.let {
                                    if (it.lowercase().equals("both")) {
                                        tv_select.visibility = View.GONE
                                        card_create_candidate.visibility = View.GONE
                                        card_create_referrer.visibility = View.GONE
                                        card_candidate.visibility = View.VISIBLE
                                        card_referrer.visibility = View.VISIBLE
                                    }
                                    else {
                                        when (role) {
                                            Constants.REFERRER -> {
                                                card_candidate.visibility = View.GONE
                                                card_create_candidate.visibility = View.VISIBLE
                                                card_referrer.visibility = View.VISIBLE
                                                card_create_referrer.visibility = View.GONE
                                            }
                                            Constants.CANDIDATE -> {
                                                card_candidate.visibility = View.VISIBLE
                                                card_create_candidate.visibility = View.GONE
                                                card_referrer.visibility = View.GONE
                                                card_create_referrer.visibility = View.VISIBLE
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                response.body()?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                else{
                    val errorResponse = Gson().fromJson(response.errorBody()?.charStream(), ResponseModel::class.java)
                    Toast.makeText(requireContext(), errorResponse.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Toast.makeText(requireContext(), "Couldn't connect to servers!", Toast.LENGTH_SHORT)
                    .show()
                t.printStackTrace()
            }
        })
    }
}