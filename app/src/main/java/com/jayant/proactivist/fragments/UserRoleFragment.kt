package com.jayant.proactivist.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.jayant.proactivist.R
import com.jayant.proactivist.activities.WelcomeActivity
import com.jayant.proactivist.utils.Constants.CANDIDATE
import com.jayant.proactivist.utils.Constants.REFERRER

class UserRoleFragment : Fragment() {
    private val TAG = UserRoleFragment::class.java.simpleName

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    lateinit var userRoleSelected: UserRoleSelected

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            userRoleSelected = context as UserRoleSelected
        }
        catch (e: ClassCastException){
            Log.d(TAG, "onAttach: " + e.printStackTrace() + " implement UserRoleSelected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_role, container, false)
        val card_referrer = view.findViewById<CardView>(R.id.card_referrer)
        val card_candidate = view.findViewById<CardView>(R.id.card_candidate)
        val tv_sign_out = view.findViewById<TextView>(R.id.tv_sign_out)

        card_referrer.setOnClickListener {
            userRoleSelected.userRoleSelected(REFERRER)
            Toast.makeText(context, "Referrer", Toast.LENGTH_SHORT).show()
        }

        card_candidate.setOnClickListener {
            userRoleSelected.userRoleSelected(CANDIDATE)
            Toast.makeText(context, "Candidate", Toast.LENGTH_SHORT).show()
        }

        tv_sign_out.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

            mGoogleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(context, WelcomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                else {
                    Toast.makeText(context, "Sign out failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(param1: String?, param2: String?): UserRoleFragment {
            val fragment = UserRoleFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    interface UserRoleSelected{
        fun userRoleSelected(role: String)
    }
}