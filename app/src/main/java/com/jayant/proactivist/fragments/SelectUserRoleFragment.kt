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
import com.jayant.proactivist.R
import com.jayant.proactivist.activities.EditProfileActivity
import com.jayant.proactivist.activities.HomeActivity
import com.jayant.proactivist.activities.WelcomeActivity
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectUserRoleFragment(var role: String) : BottomSheetDialogFragment() {

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

        isCancelable = false
        apiService = ApiUtils.getAPIService()
        val view = inflater.inflate(R.layout.fragment_select_user_role, container, false)
        card_referrer = view.findViewById(R.id.card_referrer)
        card_candidate = view.findViewById(R.id.card_candidate)
        card_create_referrer = view.findViewById(R.id.card_create_referrer)
        card_create_candidate = view.findViewById(R.id.card_create_candidate)
        tv_select = view.findViewById(R.id.tv_select)
        tv_sign_out = view.findViewById<TextView>(R.id.tv_sign_out)

        when (role) {
            Constants.REFERRER -> {
                card_candidate.visibility = View.GONE
                card_create_referrer.visibility = View.GONE
            }
            Constants.CANDIDATE -> {
                card_referrer.visibility = View.GONE
                card_create_candidate.visibility = View.GONE
            }
            else -> {
                tv_select.visibility = View.GONE
                card_create_candidate.visibility = View.GONE
                card_create_referrer.visibility = View.GONE
            }
        }

        card_create_candidate.setOnClickListener {
            val i = Intent(requireContext(), EditProfileActivity::class.java)
            i.putExtra("role", Constants.CANDIDATE)
            startActivity(i)
            activity?.finish()
        }
        card_create_referrer.setOnClickListener {
            val i = Intent(requireContext(), EditProfileActivity::class.java)
            i.putExtra("role", Constants.REFERRER)
            startActivity(i)
            activity?.finish()
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
            FirebaseAuth.getInstance().signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

            mGoogleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(requireContext(), WelcomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                else {
                    Toast.makeText(requireContext(), "Sign out failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }
}