package com.jayant.proactivists.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.chip.Chip
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jayant.proactivists.R
import com.jayant.proactivists.adapters.CompanySearchAdapter
import com.jayant.proactivists.adapters.CompanySelected
import com.jayant.proactivists.fragments.BackPressListener
import com.jayant.proactivists.fragments.EditProfileFragment
import com.jayant.proactivists.fragments.NoInternetFragment
import com.jayant.proactivists.models.CompanyItem
import com.jayant.proactivists.models.CompanySuggestion
import com.jayant.proactivists.models.ResponseModel
import com.jayant.proactivists.models.Profile
import com.jayant.proactivists.rest.APIService
import com.jayant.proactivists.rest.ApiUtils
import com.jayant.proactivists.rest.CompanySearchAPIService
import com.jayant.proactivists.utils.Constants
import com.jayant.proactivists.utils.NetworkManager
import com.jayant.proactivists.utils.PrefManager
import com.jayant.proactivists.utils.ProfilePhotoLoader
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileActivity : AppCompatActivity(), CompanySelected {

    private lateinit var flSkills: FlexboxLayout
    private lateinit var profile: Profile
    private var fromEdit = false
    private val TAG = EditProfileFragment::class.java.simpleName

    private var companyLogo = ""

    private var about = ""
    private var role = ""
    private var comp_id = ""
    private var skills = ""
    private var position = ""

    lateinit var companySearchApiService: CompanySearchAPIService
    lateinit var apiService: APIService
    private var suggestions = ArrayList<CompanySuggestion>()

    private var skillsList = arrayListOf<String>()

    private lateinit var cardSave: CardView
    private var etName: EditText? = null
    private var etExperience: EditText? = null
    private var tvExperience: TextView? = null
    private var tvSummary: TextView? = null
    private var tvSkills: TextView? = null
    private var etEmail: EditText? = null
    private var etPhone: EditText? = null
    private var etCompany: AutoCompleteTextView? = null
    private var tvPosition: TextView? = null
    private var etPosition: EditText? = null
    private var etSummary: EditText? = null
    private var etSkills: EditText? = null
    private var etPersonalLinkedin: EditText? = null
    private var etCompanyLinkedin: EditText? = null
    private var etResume: EditText? = null
    private var tvResume: TextView? = null
    private lateinit var tvProfile: TextView
    private lateinit var progress: LinearProgressIndicator
    private lateinit var ivProfile: CircleImageView
    private lateinit var backPressListener: BackPressListener
    private var runnable: Runnable? = null
    private var handler: Handler? = null
    private var timeToWait = 500L //change this one for delay (time in milli)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        handler = Handler()

        companySearchApiService = ApiUtils.getCompanySearchAPIService()
        apiService = ApiUtils.getAPIService()

        intent.extras?.getString("role", "")?.let {
            role = it
        }

        if (intent.hasExtra("fromEdit")) {
            intent.extras?.getBoolean("fromEdit", false)?.let {
                fromEdit = it
            }
        }

        if (intent.hasExtra("profile")) {
            intent.extras?.getParcelable<Profile>("profile")?.let {
                profile = it
            }
        }

        progress = findViewById(R.id.progress)
        cardSave = findViewById(R.id.card_save)
        etName = findViewById(R.id.et_name)
        etExperience = findViewById(R.id.et_experience)
        tvExperience = findViewById(R.id.tv_experience)
        etEmail = findViewById(R.id.et_email)
        etPhone = findViewById(R.id.et_phone)
        etSummary = findViewById(R.id.et_summary)
        etCompany = findViewById(R.id.et_company)
        tvPosition = findViewById(R.id.tv_position)
        etPosition = findViewById(R.id.et_position)
        etSkills = findViewById(R.id.et_skills)
        etPersonalLinkedin = findViewById(R.id.et_personal_linkedin)
        etCompanyLinkedin = findViewById(R.id.et_company_linkedin)
        etResume = findViewById(R.id.et_resume)
        tvResume = findViewById(R.id.tv_resume)
        tvProfile = findViewById(R.id.tvProfile)
        tvSkills = findViewById(R.id.tv_skills)
        tvSummary = findViewById(R.id.tv_summary)
        ivProfile = findViewById(R.id.iv_profile)
        flSkills = findViewById<FlexboxLayout>(R.id.fl_skills)

        progress.visibility = View.GONE

        if (role == Constants.REFERRER) {
            flSkills.visibility = View.GONE
            etSummary?.visibility = View.GONE
            etExperience?.visibility = View.GONE
            tvExperience?.visibility = View.GONE
            tvSkills?.visibility = View.GONE
            tvSummary?.visibility = View.GONE
            tvResume?.visibility = View.GONE
            etResume?.visibility = View.GONE
        }

        setUpWidgets()
    }

    private fun setUpWidgets() {

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {

            Glide.with(this).load(account.photoUrl.toString())
                .apply(RequestOptions.circleCropTransform())
                .placeholder(ContextCompat.getDrawable(this, R.drawable.ic_profile))
                .into(ivProfile)

//                ProfilePhotoLoader.load(
//                    this,
//                    account.photoUrl.toString(),
//                    account.displayName?.subSequence(0, 1).toString(),
//                    ivProfile,
//                    tvProfile
//                )

            etName?.setText(account.displayName)
            etEmail?.setText(account.email)
        }

        if (fromEdit) {
            profile.let {
                etName?.setText(it.name)
                etPersonalLinkedin?.setText(it.personal_linkedin)
                etEmail?.setText(it.email)
                etPhone?.setText(it.phone)
                etCompany?.setText(it.company_name)
                etCompanyLinkedin?.setText(it.company_linkedin)
                it.company_logo?.let {
                    companyLogo = it
                }
                try {
                    Glide.with(this).load(profile.photo)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(ContextCompat.getDrawable(this, R.drawable.ic_profile))
                        .into(ivProfile)

//                    ProfilePhotoLoader.load(
//                        this,
//                        profile.photo,
//                        profile.name?.subSequence(0, 1).toString(),
//                        ivProfile,
//                        tvProfile
//                    )

                } catch (e: Exception) {
                    e.printStackTrace()
                }
                it.experience?.let {
                    etExperience?.setText(it)
                }
                it.role?.let {
                    role = it
                }
                it.position?.let {
                    position = it
                    etPosition?.setText(it)
                }
                it.about?.let {
                    about = it
                    etSummary?.setText(about)
                }
                it.skills?.let {
                    skills = it
                }
                it.resume?.let {
                    etResume?.setText(it)
                }

                val type = object : TypeToken<ArrayList<String>>() {}.type
                val skills = Gson().fromJson<ArrayList<String>>(profile.skills, type)
                skills?.let {
                    it.forEach { skill ->
                        addNewChip(skill, flSkills)
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
                runnable?.let { handler?.removeCallbacks(it) }
            }

            override fun afterTextChanged(p0: Editable?) {

                runnable = Runnable {
                    //check if it is not empty then search
                    if (etCompany?.text.toString().isEmpty().not()) {
                        progress.show()
                        searchForCompany(etCompany?.text.toString())
                    }
                }
                handler?.postDelayed(runnable!!, timeToWait)
            }

        }
        )

        etSkills?.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                val txtVal = v.text
                if (!txtVal.isNullOrEmpty()) {
                    if (!skillsList.contains(txtVal.toString())) {
                        addNewChip(txtVal.toString(), flSkills)
                        etSkills?.setText("")
                    } else {
                        Snackbar.make(flSkills, "Skill already added", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }

                return@OnEditorActionListener true
            }
            false
        })

        cardSave.setOnClickListener {

            if (NetworkManager.getConnectivityStatusString(this@EditProfileActivity) != Constants.NO_INTERNET)
            {
                if (role == Constants.CANDIDATE)
                {
                    about = etSummary?.text.toString()
                    if (skillsList.isNotEmpty()) {
                        skills = Gson().toJson(skillsList)
                    }
                }
                val mAuth = FirebaseAuth.getInstance()
                val currentUser: FirebaseUser? = mAuth.currentUser
                if (currentUser != null) {
                    if (role == Constants.REFERRER)
                    {

                        if (etName?.text.toString().isNotEmpty()
                            and etPhone?.text.toString().isNotEmpty()
                            and etEmail?.text.toString().isNotEmpty()
                            and etPosition?.text.toString().isNotEmpty()
                            and etPersonalLinkedin?.text.toString().isNotEmpty()
                            and (etPersonalLinkedin?.text?.length != 28)
                            and etCompany?.text.toString().isNotEmpty()
                            and etCompanyLinkedin?.text.toString().isNotEmpty()
                            and (etCompanyLinkedin?.text?.length != 33)
                        ) {
                            val profile = Profile()
                            val company = CompanyItem(
                                etCompany?.text.toString(),
                                companyLogo,
                                etCompanyLinkedin?.text.toString()
                            )
                            profile.uid = currentUser.uid
                            profile.name = etName?.text.toString()
                            profile.phone = etPhone?.text.toString()
                            profile.email = etEmail?.text.toString()
                            profile.position = etPosition?.text.toString()
                            profile.experience = etExperience?.text.toString()
                            profile.personal_linkedin = etPersonalLinkedin?.text.toString()
                            profile.company_name = etCompany?.text.toString()
                            profile.company_linkedin = etCompanyLinkedin?.text.toString()
                            profile.skills = skills
                            profile.about = about
                            profile.role = role
                            profile.resume = etResume?.text.toString()
                            if (currentUser.photoUrl != null) {
                                profile.photo = currentUser.photoUrl.toString()
                            }
                            profile.company_logo = companyLogo
                            val prefManager = PrefManager(this)
                            prefManager.compId?.let {
                                comp_id = it
                            }
                            profile.comp_id = comp_id
                            prefManager.setProfile(profile)
                            createProfile(profile, company)
                        } else {
                            Snackbar.make(cardSave, "Incomplete details!", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                    else
                    {
                        if (etName?.text.toString().isNotEmpty()
                            and etPhone?.text.toString().isNotEmpty()
                            and etEmail?.text.toString().isNotEmpty()
//                            and etPosition?.text.toString().isNotEmpty()
//                            and etExperience?.text.toString().isNotEmpty()
                            and etPersonalLinkedin?.text.toString().isNotEmpty()
                            and (etPersonalLinkedin?.text?.length != 28)
//                            and etCompany?.text.toString().isNotEmpty()
//                            and etCompanyLinkedin?.text.toString().isNotEmpty()
//                            and (etCompanyLinkedin?.text?.length != 33)
                            and etResume?.text.toString().isNotEmpty()
                            and etResume?.text.toString().contains("drive.google.com")
                        ) {
                            val profile = Profile()
                            val company = CompanyItem(
                                etCompany?.text.toString(),
                                companyLogo,
                                etCompanyLinkedin?.text.toString()
                            )
                            profile.uid = currentUser.uid
                            profile.name = etName?.text.toString()
                            profile.phone = etPhone?.text.toString()
                            profile.email = etEmail?.text.toString()
                            profile.position = etPosition?.text.toString()
                            profile.experience = etExperience?.text.toString()
                            profile.personal_linkedin = etPersonalLinkedin?.text.toString()
                            profile.company_name = etCompany?.text.toString()
                            profile.company_linkedin = etCompanyLinkedin?.text.toString()
                            profile.skills = skills
                            profile.about = about
                            profile.role = role
                            profile.resume = etResume?.text.toString()
                            if (currentUser.photoUrl != null) {
                                profile.photo = currentUser.photoUrl.toString()
                            }
                            profile.company_logo = companyLogo
                            val prefManager = PrefManager(this)
                            prefManager.compId?.let {
                                comp_id = it
                            }
                            profile.comp_id = comp_id
                            prefManager.setProfile(profile)
                            createProfile(profile, company)

                        }
                        else {
                            Snackbar.make(cardSave, "Incomplete details!", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
            else {
                val fragment = NoInternetFragment()
                fragment.show(supportFragmentManager, "")
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
            val chip = Chip(this)
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
            companyLogo = companySuggestion.logo
        }
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
                        Log.d(TAG, "onResponse: " + response.body())
                        response.body()?.forEach {
                            suggestions.add(it)
                        }
                        val companySearchAdapter = CompanySearchAdapter(
                            this@EditProfileActivity,
                            R.layout.card_layout_job,
                            suggestions,
                            this@EditProfileActivity
                        )
                        etCompany?.setAdapter(companySearchAdapter)

                        companySearchAdapter.notifyDataSetChanged()

                        progress.visibility = View.GONE
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

    private fun createProfile(profile: Profile, company: CompanyItem) {
        var role = ""
        profile.role?.let {
            role = it
        }

        val jsonObject = JSONObject()
        val jsonObjectProfile = JSONObject()
        val jsonObjectCompany = JSONObject()
        jsonObjectProfile.put("uid", profile.uid)
        jsonObjectProfile.put("name", profile.name)
        jsonObjectProfile.put("phone", profile.phone)
        jsonObjectProfile.put("email", profile.email)
        jsonObjectProfile.put("photo", profile.photo)
        jsonObjectProfile.put("about", profile.about)
        jsonObjectProfile.put("skills", profile.skills)
        jsonObjectProfile.put("experience", profile.experience)
        jsonObjectProfile.put("position", profile.position)
        jsonObjectProfile.put("personal_linkedin", profile.personal_linkedin)
        jsonObjectProfile.put("role", profile.role)
        jsonObjectProfile.put("resume", profile.resume)
        jsonObjectProfile.put("comp_id", comp_id)

        jsonObjectCompany.put("company_name", company.company_name)
        jsonObjectCompany.put("company_logo", company.company_logo)
        jsonObjectCompany.put("company_linkedin", company.company_linkedin)
        jsonObject.put("profile", jsonObjectProfile)
        jsonObject.put("company", jsonObjectCompany)

        if (fromEdit) {
            val body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonObject.toString()
            )
            profile.uid?.let {
                apiService.updateProfile(it, role + "s", body).enqueue(object : Callback<ResponseModel> {
                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {
                        if (response.isSuccessful) {
                            try {
                                if(response.code() == 200) {
                                    finish()
                                }

                                else {
                                    Toast.makeText(
                                        this@EditProfileActivity,
                                        response.body()?.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                        Log.d("profile_backend", "onFailure: ${call.request().url()}")
                        t.printStackTrace()
                    }

                })
            }

        } else {
            val body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonObject.toString()
            )

            apiService.createProfile(body).enqueue(object : Callback<ResponseModel> {
                override fun onResponse(
                    call: Call<ResponseModel>,
                    response: Response<ResponseModel>
                ) {
                    if (response.isSuccessful) {
                        try {
                            if(response.code() == 200) {
                                val i = Intent(this@EditProfileActivity, HomeActivity::class.java)
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(i)
                                finish()
                            }
                            else {
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    response.body()?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                    Log.d("profile_backend", "onFailure: ${call.request().url()}")
                    t.printStackTrace()
                }

            })
        }
    }
}