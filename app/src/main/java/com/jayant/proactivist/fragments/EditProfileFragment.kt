package com.jayant.proactivist.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jayant.proactivist.R
import com.jayant.proactivist.activities.ContainerActivity
import com.jayant.proactivist.activities.HomeActivity
import com.jayant.proactivist.adapters.CompanySearchAdapter
import com.jayant.proactivist.adapters.CompanySelected
import com.jayant.proactivist.models.CompanySuggestion
import com.jayant.proactivist.models.Profile
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.rest.CompanySearchAPIService
import com.jayant.proactivist.utils.Constants.CANDIDATE
import com.jayant.proactivist.utils.Constants.REFERRER
import com.jayant.proactivist.utils.PrefManager
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class EditProfileFragment(var role: String) : Fragment(), CompanySelected {

    private lateinit var fl_skills: FlexboxLayout
    private lateinit var profile: Profile
    private var fromEdit = false

    constructor(profile: Profile, role: String) : this(role) {
        this.profile = profile
        fromEdit = true
    }
    private var company_logo = ""
    private val TAG = EditProfileFragment::class.java.simpleName

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var summary = ""
    private var position = ""
    private var skills = ""

    lateinit var apiService: CompanySearchAPIService
    private var suggestions = ArrayList<CompanySuggestion>()

    private var skillsList = arrayListOf<String>()

    private lateinit var card_save: CardView
    private var etName: EditText? = null
    private var etExperience: EditText? = null
    private var tv_experience: TextView? = null
    private var tv_summary: TextView? = null
    private var tv_skills: TextView? = null
    private var etEmail: EditText? = null
    private var etPhone: EditText? = null
    private var etCompany: AutoCompleteTextView? = null
    private var tvPosition: TextView? = null
    private var etPosition: EditText? = null
    private var etSummary: EditText? = null
    private var et_skills: EditText? = null
    private var et_personal_linkedin: EditText? = null
    private var et_company_linkedin: EditText? = null
    private var et_resume: EditText? = null
    private var tv_resume: TextView? = null
    private lateinit var iv_profile: CircleImageView
    private lateinit var backPressListener: BackPressListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            backPressListener = context as BackPressListener
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        apiService = ApiUtils.getCompanySearchAPIService()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        card_save = view.findViewById(R.id.card_save)
        etName = view.findViewById(R.id.et_name)
        etExperience = view.findViewById(R.id.et_experience)
        tv_experience = view.findViewById(R.id.tv_experience)
        etEmail = view.findViewById(R.id.et_email)
        etPhone = view.findViewById(R.id.et_phone)
        etSummary = view.findViewById(R.id.et_summary)
        etCompany = view.findViewById(R.id.et_company)
        et_skills = view.findViewById(R.id.et_skills)
        et_personal_linkedin = view.findViewById(R.id.et_personal_linkedin)
        et_company_linkedin = view.findViewById(R.id.et_company_linkedin)
        et_resume = view.findViewById(R.id.et_resume)
        tv_resume = view.findViewById(R.id.tv_resume)
        tv_skills = view.findViewById(R.id.tv_skills)
        tv_summary = view.findViewById(R.id.tv_summary)
        iv_profile = view.findViewById(R.id.iv_profile)
        fl_skills = view.findViewById<FlexboxLayout>(R.id.fl_skills)

        if (role == REFERRER) {
            fl_skills.visibility = View.GONE
            etSummary?.visibility = View.GONE
            etExperience?.visibility = View.GONE
            tv_experience?.visibility = View.GONE
            tv_skills?.visibility = View.GONE
            tv_summary?.visibility = View.GONE
            tv_resume?.visibility = View.GONE
            et_resume?.visibility = View.GONE
        }
        else{
            tvPosition?.visibility = View.GONE
            etPosition?.visibility = View.GONE
        }

        return view
    }


    private fun searchForCompany(query: String) {

        suggestions.clear()

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
                        context,
                        R.layout.card_layout_job,
                        suggestions,
                        this@EditProfileFragment
                    )
                    etCompany?.setAdapter(companySearchAdapter)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "handleOnBackPressed: ")
                backPressListener.backPressed()
            }

        })

        try {
            setUpWidgets()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun setUpWidgets() {

        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (account != null) {
            if (account.photoUrl != null) {
                Glide.with(this)
                    .load(account.photoUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(iv_profile)
            }
            etName?.setText(account.displayName)
            etEmail?.setText(account.email)
        }

        if(fromEdit) {
            profile.let {
                etName?.setText(it.name)
                et_personal_linkedin?.setText(it.personal_linkedin)
                etEmail?.setText(it.email)
                etPhone?.setText(it.phone)
                etCompany?.setText(it.company_name)
                et_company_linkedin?.setText(it.company_linkedin)
                it.company_logo?.let {
                    company_logo = it
                }
                try {
                    context?.let { it1 ->
                        Glide.with(it1).load(profile.photo)
                            .placeholder(ContextCompat.getDrawable(it1, R.drawable.ic_profile))
                            .into(iv_profile)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                it.experience?.let {
                    etExperience?.setText(it)
                }
                it.position?.let {
                    etPosition?.setText(it)
                }
                it.role?.let {
                    role = it
                }
                it.about?.let {
                    summary = it
                    etSummary?.setText(summary)
                }
                it.skills?.let {
                    skills = it
                }
                it.resume?.let {
                    et_resume?.setText(it)
                }

                val type = object : TypeToken<ArrayList<String>>() {}.type
                val skills = Gson().fromJson<ArrayList<String>>(profile.skills, type)
                skills?.let {
                    it.forEach { skill ->
                        addNewChip(skill, fl_skills)
                    }
                }

            }
        }

        Log.d(TAG, "onCreateView etPhone: ${etPhone?.isInEditMode}")
        Log.d(TAG, "onCreateView etPhone: ${etPhone?.editableText}")

        etCompany?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchForCompany(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        }
        )

        et_skills?.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                val txtVal = v.text
                if (!txtVal.isNullOrEmpty()) {
                    if (!skillsList.contains(txtVal.toString())) {
                        addNewChip(txtVal.toString(), fl_skills)
                        et_skills?.setText("")
                    } else {
                        view?.let { Snackbar.make(it, "Skill already added", Snackbar.LENGTH_SHORT).show() }
                    }
                }

                return@OnEditorActionListener true
            }
            false
        })

        card_save.setOnClickListener {
            if (role == CANDIDATE) {
                summary = etSummary?.text.toString()
                if (skillsList.isNotEmpty()) {
                    skills = Gson().toJson(skillsList)
                }
            }
            Log.d(TAG, "onCreateView fromEdit: $fromEdit")
            val database = FirebaseDatabase.getInstance()
            val mAuth = FirebaseAuth.getInstance()
            val currentUser: FirebaseUser? = mAuth.currentUser
            if (currentUser != null) {
                if (role == REFERRER) {

                    if (etName?.text.toString().isNotEmpty() and etPhone?.text.toString()
                            .isNotEmpty() and etEmail?.text.toString().isNotEmpty()
                        and et_personal_linkedin?.text.toString().isNotEmpty()
                        and etCompany?.text.toString().isNotEmpty()
                        and et_company_linkedin?.text.toString().isNotEmpty()
                    ) {
                        val profile = Profile()
                        profile.uid = currentUser.uid
                        profile.name = etName?.text.toString()
                        profile.phone = etPhone?.text.toString()
                        profile.email = etEmail?.text.toString()
                        profile.experience = etExperience?.text.toString()
                        profile.position = etPosition?.text.toString()
                        profile.personal_linkedin = et_personal_linkedin?.text.toString()
                        profile.company_name = etCompany?.text.toString()
                        profile.company_linkedin = et_company_linkedin?.text.toString()
                        profile.skills = skills
                        profile.about = summary
                        profile.role = role
                        profile.resume = et_resume?.text.toString()
                        if (currentUser.photoUrl != null) {
                            profile.photo = currentUser.photoUrl.toString()
                        }
                        profile.company_logo = company_logo
                        context?.let {
                            val prefManager = PrefManager(it)
                            prefManager.setProfile(profile)
                        }
                        database.reference.child("companies").child(etCompany?.text.toString()).push()
                            .setValue(currentUser.uid).addOnFailureListener(object : OnFailureListener{
                                override fun onFailure(p0: Exception) {
                                    p0.printStackTrace()
                                }

                            })
                        database.reference.child("users").child(currentUser.uid).child("profile")
                            .setValue(profile).addOnFailureListener(object : OnFailureListener{
                                override fun onFailure(p0: Exception) {
                                    p0.printStackTrace()
                                }

                            })

                        if(!this.fromEdit){
                            backPressListener.backPressed()
                        }
                        else{
                            val i = Intent(context, HomeActivity::class.java)
                            startActivity(i)
                            (context as ContainerActivity).finish()
                        }

                    }
                    else {
                        Snackbar.make(card_save, "Incomplete details!", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }

                else{

                    if (etName?.text.toString().isNotEmpty() and etPhone?.text.toString()
                            .isNotEmpty() and etEmail?.text.toString().isNotEmpty()
                        and etExperience?.text.toString()
                            .isNotEmpty() and et_personal_linkedin?.text.toString().isNotEmpty()
                        and etCompany?.text.toString()
                            .isNotEmpty() and et_company_linkedin?.text.toString().isNotEmpty()
                        and et_resume?.text.toString().isNotEmpty()
                    ) {
                        val profile = Profile()
                        profile.uid = currentUser.uid
                        profile.name = etName?.text.toString()
                        profile.phone = etPhone?.text.toString()
                        profile.email = etEmail?.text.toString()
                        profile.experience = etExperience?.text.toString()
                        profile.position = etPosition?.text.toString()
                        profile.personal_linkedin = et_personal_linkedin?.text.toString()
                        profile.company_name = etCompany?.text.toString()
                        profile.company_linkedin = et_company_linkedin?.text.toString()
                        profile.skills = skills
                        profile.about = summary
                        profile.role = role
                        profile.resume = et_resume?.text.toString()
                        if (currentUser.photoUrl != null) {
                            profile.photo = currentUser.photoUrl.toString()
                        }
                        profile.company_logo = company_logo
                        context?.let {
                            val prefManager = PrefManager(it)
                            prefManager.setProfile(profile)
                        }

                        database.reference.child("users").child(currentUser.uid).child("profile")
                            .setValue(profile).addOnFailureListener(object : OnFailureListener{
                                override fun onFailure(p0: Exception) {
                                    p0.printStackTrace()
                                }

                            })

                        Log.d(TAG, "onCreateView: on edit: " + this.fromEdit)
                        if(this.fromEdit){
                            Log.d(TAG, "onCreateView: on edit false")
                            backPressListener.backPressed()
                        }
                        else {
                            val i = Intent(context, HomeActivity::class.java)
                            startActivity(i)
                            (context as ContainerActivity).finish()
                        }

                    } else {
                        Snackbar.make(card_save, "Incomplete details!", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun addNewChip(skill: String, chipGroup: FlexboxLayout) {
        if (!skillsList.contains(skill)) {

            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 10, 0)
            skillsList.add(skill)
            val chip = Chip(context)
            chip.text = skill
            chip.isCloseIconEnabled = true
            chip.isClickable = true
            chip.isCheckable = false
            chipGroup.addView(chip as View, chipGroup.childCount - 1, layoutParams)
            chip.setOnCloseIconClickListener {
                chipGroup.removeView(chip as View)
                skillsList.remove(skill)
            }
        }
    }

    override fun companySelected(companySuggestion: CompanySuggestion?) {
        if (companySuggestion != null) {
            etCompany?.setText(companySuggestion.name)
            etCompany?.dismissDropDown()
            company_logo = companySuggestion.logo
        }
    }

}

interface BackPressListener {
    fun backPressed()
}