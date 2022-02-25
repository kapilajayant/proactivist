package com.jayant.proactivist.fragments

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.jayant.proactivist.activities.HelpActivity
import com.jayant.proactivist.R
import com.jayant.proactivist.activities.*
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.utils.*
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
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var tv_role: TextView
    private lateinit var tv_switch: TextView
    private lateinit var btn_sign_out: TextView
    private lateinit var tv_review: CardView
    private lateinit var tv_about_us: CardView
    private lateinit var card_chat: CardView
    private lateinit var card_coins: CardView
    private lateinit var card_help: CardView
    private lateinit var card_invite: CardView
    private lateinit var card_profile: CardView
    private lateinit var card_top: CardView
    private var inviteCode = ""
    private lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
        apiService = ApiUtils.getAPIService()
        prefManager = PrefManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        tv_profile_name = view.findViewById(R.id.tv_profile_name)
        swipe = view.findViewById(R.id.swipe)
        tv_role = view.findViewById(R.id.tv_role)
        tv_switch = view.findViewById(R.id.tv_switch)
        iv_profile = view.findViewById(R.id.iv_profile)
        card_chat = view.findViewById(R.id.card_chat)
        card_coins = view.findViewById(R.id.card_coins)
        card_help = view.findViewById(R.id.card_help)
        card_invite = view.findViewById(R.id.card_invite)
        card_profile = view.findViewById(R.id.card_profile)
        card_top = view.findViewById(R.id.card_top)
        tv_review = view.findViewById(R.id.tv_review)
        tv_about_us = view.findViewById(R.id.tv_about_us)
        btn_sign_out = view.findViewById(R.id.btn_sign_out)

        swipe.setOnRefreshListener {
            getCoins()
            setupProfile()
            val currentUser = FirebaseAuth.getInstance().currentUser
            val uid = currentUser?.uid
            val role = prefManager.profileRole
            if (role != null && uid != null) {
                switchProfile(role, uid)
            }
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid
        val role = prefManager.profileRole
        if (role != null && uid != null) {
            switchProfile(role, uid)
        }

        getCoins()
        setupProfile()


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        val mAuth = FirebaseAuth.getInstance()

        btn_sign_out.setOnClickListener {
            mGoogleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ShortcutManagerUtil.removeShortcuts(requireContext())
                    mAuth.signOut()
                    val guideCount = prefManager.guideCount
                    PrefManager(requireContext()).clear()
                    prefManager.guideCount = guideCount
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
        tv_switch.setOnClickListener{

            val currentUser = FirebaseAuth.getInstance().currentUser
            val uid = currentUser?.uid
            val role = prefManager.profileRole
            if (role != null && uid != null) {
                switchProfile(role, uid, true)
            }

        }
        card_profile.setOnLongClickListener {
            if (NetworkManager.getConnectivityStatusString(requireContext()) != Constants.NO_INTERNET) {
                val role = prefManager.profileRole
                val fragment = role?.let { it1 -> SelectUserRoleFragment(it1, true) }
                fragment?.show(parentFragmentManager, "")
            }
            else {
                val fragment = NoInternetFragment()
                fragment.show(parentFragmentManager, "")
            }
            return@setOnLongClickListener true
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

        var guideCount = prefManager.guideCount
        GuideManager.showGuide(requireContext(), card_top, "My Profile", "View and edit your profile or long press to view your roles", 4) {
            guideCount++
            prefManager.guideCount = guideCount
            GuideManager.showGuide(requireContext(), tv_switch, "Switch modes", "Tap here to switch your mode", 5) {
                guideCount++
                prefManager.guideCount = guideCount
                GuideManager.showGuide(requireContext(), tv_review, "Feedback", "Tap here for reviews, feedback or issue", 6) {
                    guideCount++
                    prefManager.guideCount = guideCount
                }
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
                    swipe.isRefreshing = false
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
                    swipe.isRefreshing = false
                    Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show()
                    Log.d("profile_backend", "onFailure: ${call.request().url()}")
                    t.printStackTrace()
                }

            })
        }
    }
    private fun switchProfile(role: String, uid: String, switch: Boolean = false) {
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
                                        when (role) {
                                            Constants.REFERRER -> {
                                                tv_switch.visibility = View.VISIBLE
                                                tv_switch.text = "Switch to candidate mode"
                                                if(switch) {
                                                    val prefManager = PrefManager(requireContext())
                                                    prefManager.profileRole = Constants.CANDIDATE
                                                    val i = Intent(
                                                        requireContext(),
                                                        HomeActivity::class.java
                                                    )
                                                    startActivity(i)
                                                    activity?.finish()
                                                }
                                                else{
                                                    animate(tv_switch)
                                                }
                                            }
                                            Constants.CANDIDATE -> {
                                                tv_switch.visibility = View.VISIBLE
                                                tv_switch.text = "Switch to referrer mode"
                                                if(switch) {
                                                    val prefManager = PrefManager(requireContext())
                                                    prefManager.profileRole = Constants.REFERRER
                                                    val i = Intent(
                                                        requireContext(),
                                                        HomeActivity::class.java
                                                    )
                                                    startActivity(i)
                                                    activity?.finish()
                                                }
                                                else{
                                                    animate(tv_switch)
                                                }
                                            }
                                            else ->{
                                                tv_switch.visibility = View.GONE
                                                card_profile.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                                                    setMargins(0, 0, 0, 0)
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        tv_switch.visibility = View.GONE
                                        card_profile.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                                            setMargins(0, 0, 0, 0)
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
                    try {
                        val errorResponse = Gson().fromJson(response.errorBody()?.charStream(), ResponseModel::class.java)
                        Toast.makeText(requireContext(), errorResponse.message, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Toast.makeText(requireContext(), "Couldn't connect to servers!", Toast.LENGTH_SHORT)
                    .show()
                t.printStackTrace()
            }
        })
    }

    private fun animate(view: TextView){
        val duration = 500L
        val animator = AnimatorSet()
        val alphaAnimPro = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
        val animAlpha = ObjectAnimator.ofPropertyValuesHolder(view, alphaAnimPro)

        val animBottomToTop = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, dpToPx(10), 0f)
        val animBottomToTopCard = ObjectAnimator.ofFloat(card_profile, View.TRANSLATION_Y, dpToPx(10), 0f)

        animBottomToTop.duration = duration
        animator.playTogether(animAlpha, animBottomToTop, animAlpha, animBottomToTopCard)
        animator.start()
        animator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(p0: Animator?) {
                card_top.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            }

            override fun onAnimationEnd(p0: Animator?) {
                card_top.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }

        })
    }

    private fun dpToPx(dp: Int): Float{
        return (dp * Resources.getSystem().displayMetrics.density)
    }
}