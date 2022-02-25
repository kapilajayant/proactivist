package com.jayant.proactivist.activities

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
import com.jayant.proactivist.R
import com.jayant.proactivist.adapters.CompanySearchAdapter
import com.jayant.proactivist.adapters.CompanySelected
import com.jayant.proactivist.fragments.BackPressListener
import com.jayant.proactivist.fragments.EditProfileFragment
import com.jayant.proactivist.fragments.NoInternetFragment
import com.jayant.proactivist.models.CompanyItem
import com.jayant.proactivist.models.CompanySuggestion
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.models.Profile
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.rest.CompanySearchAPIService
import com.jayant.proactivist.utils.*
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.jayant.proactivist.fragments.SearchCompanyFragment

class EditProfileActivity : AppCompatActivity(), CompanySelected {

    private lateinit var companySearchAdapter: CompanySearchAdapter
    private lateinit var searchCompanyFragment: SearchCompanyFragment
    private var years = 0
    private var months = 0
    private lateinit var flSkills: FlexboxLayout
    private var profile: Profile? = null
    private var fromEdit = false
    private var switch = false
    private val TAG = EditProfileFragment::class.java.simpleName

    private var companyLogo = ""
    private var companyName = ""

    private var about = ""
    private var role = ""
    private var comp_id = ""
    private var skills = ""
    private var position = ""

    lateinit var companySearchApiService: CompanySearchAPIService
    lateinit var apiService: APIService
    private var suggestions = ArrayList<CompanySuggestion>()
    private var skillsSuggestions = ArrayList<String>()

    private var skillsList = arrayListOf<String>()

    private lateinit var numberPickerYears: NumberPicker
    private lateinit var numberPickerMonths: NumberPicker
    private lateinit var tv_app_bar: TextView
    private lateinit var iv_back: ImageView
    private lateinit var cardSave: Button
    private var etName: EditText? = null
    private var etExperience: EditText? = null
    private var tvExperience: TextView? = null
    private var tvSummary: TextView? = null
    private var tvSkills: TextView? = null
    private var etEmail: EditText? = null
    private var etPhone: EditText? = null
    private var tvPosition: TextView? = null
    private var etPosition: EditText? = null
    private var etSummary: EditText? = null
    private var etSkills: AutoCompleteTextView? = null
    private var etPersonalLinkedin: EditText? = null
    private var etCompanyLinkedin: EditText? = null
    private var etResume: EditText? = null
    private var tvResume: TextView? = null
    private lateinit var tvProfile: TextView
    private lateinit var ivProfile: CircleImageView
    private lateinit var iv_logo: ImageView
    private lateinit var tv_company_name: TextView
    private lateinit var constraint_company: ConstraintLayout
    private lateinit var backPressListener: BackPressListener
    private var runnable: Runnable? = null
    private var handler: Handler? = null
    private var timeToWait = 500L //change this one for delay (time in milli)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        skillsSuggestions.add("React")
        skillsSuggestions.add("Java")
        skillsSuggestions.add("Android")
        skillsSuggestions.add("Kotlin")
        skillsSuggestions.add("php")
        skillsSuggestions.add("ios")
        skillsSuggestions.add("Python")
        skillsSuggestions.add("Django")
        skillsSuggestions.add("Vue")
        skillsSuggestions.add("Html")
        skillsSuggestions.add("Javascript")
        skillsSuggestions.add("Flutter")
        skillsSuggestions.add("React Native")
        skillsSuggestions.add("Angular")
        skillsSuggestions.add("C")
        skillsSuggestions.add("C++")
        skillsSuggestions.add("C#")
        skillsSuggestions.add("Go")
        skillsSuggestions.add("CSS")
        skillsSuggestions.add("Figma")
        skillsSuggestions.add("UI UX")
        skillsSuggestions.add("Adobe XD")
        skillsSuggestions.add("Graphic Design")
        skillsSuggestions.add("kafka")
        skillsSuggestions.add("Kubernetes")
        skillsSuggestions.add("Docker")
        skillsSuggestions.add("Devops")
        skillsSuggestions.add("AWS")
        skillsSuggestions.add("GCP")
        skillsSuggestions.add("Swift")
        skillsSuggestions.add("Node JS")
        skillsSuggestions.add("Mongo")
        skillsSuggestions.add("Firebase")
        skillsSuggestions.add("Socket")
        skillsSuggestions.add("Dot Net")

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

        if (intent.hasExtra("switch")) {
            intent.extras?.getBoolean("switch", false)?.let {
                switch = it
            }
        }



        if (intent.hasExtra("profile")) {
            intent.extras?.getParcelable<Profile>("profile")?.let {
                profile = it
            }
        }
        numberPickerYears = findViewById(R.id.numberPickerYears)
        numberPickerMonths = findViewById(R.id.numberPickerMonths)
        tv_app_bar = findViewById(R.id.tv_app_bar)
        iv_back = findViewById(R.id.iv_back)
        cardSave = findViewById(R.id.card_save)
        etName = findViewById(R.id.et_name)
        etExperience = findViewById(R.id.et_experience)
        tvExperience = findViewById(R.id.tv_experience)
        etEmail = findViewById(R.id.et_email)
        etPhone = findViewById(R.id.et_phone)
        etSummary = findViewById(R.id.et_summary)
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
        iv_logo = findViewById(R.id.iv_logo)
        tv_company_name = findViewById(R.id.tv_company_name)
        constraint_company = findViewById(R.id.constraint_company)
        flSkills = findViewById<FlexboxLayout>(R.id.fl_skills)
1
        setUpWidgets()
    }

    private fun setUpWidgets() {

        searchCompanyFragment = SearchCompanyFragment(this)

        numberPickerMonths.maxValue = 12
        numberPickerMonths.minValue = 0
        numberPickerMonths.value = 0
        numberPickerYears.maxValue = 50
        numberPickerYears.minValue = 0
        numberPickerYears.value = 0

        numberPickerYears.setOnValueChangedListener { numberPicker, old, new ->
            years = new
        }

        numberPickerMonths.setOnValueChangedListener { numberPicker, old, new ->
            months = new
        }

        if (fromEdit) {
            tv_app_bar.text = "Edit Profile"
        } else {
            tv_app_bar.text = "Create Profile"
        }

        iv_back.setOnClickListener {
            finish()
        }

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

        profile?.let {

            val years = it.experience?.substringBefore(":")
            val months = it.experience?.substringAfter(":")
            if (years != null) {
                this.years = years.toInt()
                numberPickerYears.value = years.toInt()
            }
            if (months != null) {
                this.months = months.toInt()
                numberPickerMonths.value = months.toInt()
            }

            etName?.setText(it.name)
            etPersonalLinkedin?.setText(it.personal_linkedin)
            etEmail?.setText(it.email)
            etPhone?.setText(it.phone)
            it.company_name?.let {
                companyName = it
            }
            tv_company_name.text = companyName
            etCompanyLinkedin?.setText(it.company_linkedin)
            it.company_logo?.let {
                companyLogo = it
                Glide.with(this).load(it).into(iv_logo)
            }
            try {
                Glide.with(this).load(it.photo)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(ContextCompat.getDrawable(this, R.drawable.ic_profile))
                    .into(ivProfile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            it.experience?.let {
                etExperience?.setText(it)
            }
            it.role?.let {
                role = it
                if(switch) {
                    role = if (role == Constants.REFERRER) {
                        Constants.CANDIDATE
                    } else {
                        Constants.REFERRER
                    }
                }
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
            it.comp_id?.let {
                comp_id = it
            }
            it.resume?.let {
                etResume?.setText(it)
            }

            val type = object : TypeToken<ArrayList<String>>() {}.type
            val skills = Gson().fromJson<ArrayList<String>>(it.skills, type)
            skills?.let {
                it.forEach { skill ->
                    addNewChip(skill, flSkills)
                }
            }
        }

        tvResume?.setOnClickListener {
            Snackbar.make(it, "Remember to keep your resume\'s access \"Anyone with the link\"", Snackbar.LENGTH_LONG).show()
        }

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

        companySearchAdapter = CompanySearchAdapter(
            this@EditProfileActivity,
            R.layout.card_layout_job,
            suggestions,
            this@EditProfileActivity
        )
        constraint_company.setOnClickListener {
            searchCompanyFragment.show(supportFragmentManager, "")
        }

        etSkills?.threshold = 1

        val adapter = ArrayAdapter<String>(this, android.R.layout.select_dialog_item, skillsSuggestions)
        etSkills?.setOnItemClickListener { adapterView, view, i, l ->
            val skill = adapterView.adapter.getItem(i) as String
            addNewChip(skill, flSkills)
            etSkills?.setText("")
        }
        etSkills?.setAdapter(adapter) //setting the adapter data into the AutoCompleteTextView
        etSkills?.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                val txtVal = v.text
                if (!txtVal.isNullOrEmpty()) {
                    if (!skillsList.contains(txtVal.toString())) {
                        addNewChip(txtVal.toString(), flSkills)
                        etSkills?.setText("")
                    }
                    else {
                        Snackbar.make(flSkills, "Skill already added", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }

                return@OnEditorActionListener true
            }
            false
        })

        cardSave.setOnClickListener {
            if (NetworkManager.getConnectivityStatusString(this@EditProfileActivity) != Constants.NO_INTERNET) {
                saveProfile()
            }
            else {
                val fragment = NoInternetFragment()
                fragment.show(supportFragmentManager, "")
            }
        }
    }

    private fun saveProfile() {
        if (role == Constants.CANDIDATE) {
            about = etSummary?.text.toString()
            if (skillsList.isNotEmpty()) {
                skills = Gson().toJson(skillsList)
            }
        }
        val mAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = mAuth.currentUser
        if (currentUser != null) {
            if (role == Constants.REFERRER) {
                if (etName?.text.toString().isNotEmpty()) {
                    if (etPhone?.text.toString().isNotEmpty()) {
                        if (etEmail?.text.toString().isNotEmpty()) {
                            if (etPosition?.text.toString().isNotEmpty()) {
                                if (etPersonalLinkedin?.text.toString().isNotEmpty()) {
                                    if ((etPersonalLinkedin?.text?.length != 28)) {
                                        if (companyName.isNotEmpty()) {
                                            if (etCompanyLinkedin?.text.toString()
                                                    .isNotEmpty()
                                            ) {
                                                if ((etCompanyLinkedin?.text?.length != 33)) {
                                                    val profile = Profile()
                                                    val company = CompanyItem(
                                                        companyName,
                                                        companyLogo,
                                                        etCompanyLinkedin?.text.toString()
                                                    )
                                                    profile.uid = currentUser.uid
                                                    profile.name = etName?.text.toString()
                                                    profile.phone = etPhone?.text.toString()
                                                    profile.email = etEmail?.text.toString()
                                                    profile.position =
                                                        etPosition?.text.toString()
                                                    profile.experience = "$years:$months"
                                                    profile.personal_linkedin =
                                                        etPersonalLinkedin?.text.toString()
                                                    profile.company_name =
                                                        companyName
                                                    profile.company_linkedin =
                                                        etCompanyLinkedin?.text.toString()
                                                    profile.skills = skills
                                                    profile.about = about
                                                    profile.role = role
                                                    profile.resume =
                                                        etResume?.text.toString()
                                                    if (currentUser.photoUrl != null) {
                                                        profile.photo =
                                                            currentUser.photoUrl.toString()
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
                                                    Snackbar.make(
                                                        cardSave,
                                                        "Add complete complete linkedin id!",
                                                        Snackbar.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                Snackbar.make(
                                                    cardSave,
                                                    "Enter company linkedin!",
                                                    Snackbar.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            Snackbar.make(
                                                cardSave,
                                                "Enter company!",
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Snackbar.make(
                                            cardSave,
                                            "Add complete personal linkedin id!",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Snackbar.make(
                                        cardSave,
                                        "Enter personal linkedin id!",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Snackbar.make(
                                    cardSave,
                                    "Enter position!",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Snackbar.make(cardSave, "Enter email!", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Snackbar.make(cardSave, "Enter phone!", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Snackbar.make(cardSave, "Enter name!", Snackbar.LENGTH_SHORT).show()
                }
            }
            else {
                if (etName?.text.toString().isNotEmpty()) {
                    if (etPhone?.text.toString().isNotEmpty()) {
                        if (etEmail?.text.toString().isNotEmpty()) {
                            if (etPersonalLinkedin?.text.toString().isNotEmpty()) {
                                if ((etPersonalLinkedin?.text?.length != 28)) {
                                    if (etResume?.text.toString().isNotEmpty()) {
                                        if (etResume?.text.toString()
                                                .contains("drive.google.com")
                                        ) {
                                            val profile = Profile()
                                            val company = CompanyItem(
                                                companyName,
                                                companyLogo,
                                                etCompanyLinkedin?.text.toString()
                                            )
                                            profile.uid = currentUser.uid
                                            profile.name = etName?.text.toString()
                                            profile.phone = etPhone?.text.toString()
                                            profile.email = etEmail?.text.toString()
                                            profile.position = etPosition?.text.toString()
                                            profile.experience = "$years:$months"
                                            profile.personal_linkedin =
                                                etPersonalLinkedin?.text.toString()
                                            profile.company_name = companyName
                                            profile.company_linkedin =
                                                etCompanyLinkedin?.text.toString()
                                            profile.skills = skills
                                            profile.about = about
                                            profile.role = role
                                            profile.resume = etResume?.text.toString()
                                            if (currentUser.photoUrl != null) {
                                                profile.photo =
                                                    currentUser.photoUrl.toString()
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
                                            Snackbar.make(
                                                cardSave,
                                                "Add a valid google drive resume!",
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Snackbar.make(
                                            cardSave,
                                            "Add resume!",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Snackbar.make(
                                        cardSave,
                                        "Add complete personal linkedin id!",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Snackbar.make(
                                    cardSave,
                                    "Enter personal linkedin id!",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Snackbar.make(cardSave, "Enter email!", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Snackbar.make(cardSave, "Enter phone!", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Snackbar.make(cardSave, "Enter name!", Snackbar.LENGTH_SHORT).show()
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
            searchCompanyFragment.dismiss()
            companyName = companySuggestion.name
            tv_company_name.text = companySuggestion.name
            companyLogo = companySuggestion.logo
            Glide.with(this).load(companyLogo).into(iv_logo)
        }
    }

    private fun createProfile(profile: Profile, company: CompanyItem) {
        getToken(profile, company)
    }

    private fun getToken(profile: Profile, company: CompanyItem){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            else {
                val token = task.result
                var role = ""
                profile.role?.let {
                    role = it
                }

                DialogHelper.showLoadingDialog(this)

                val jsonObject = JSONObject()
                val jsonObjectProfile = JSONObject()
                val jsonObjectCompany = JSONObject()
                jsonObjectProfile.put("uid", profile.uid)
                jsonObjectProfile.put("token", token)
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
                        apiService.updateProfile(it, role + "s", body)
                            .enqueue(object : Callback<ResponseModel> {
                                override fun onResponse(
                                    call: Call<ResponseModel>,
                                    response: Response<ResponseModel>
                                ) {
                                    if (response.isSuccessful) {
                                        try {
                                            if (response.code() == 200) {
                                                finish()
                                            } else {
                                                Toast.makeText(
                                                    this@EditProfileActivity,
                                                    response.body()?.message,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            DialogHelper.hideLoadingDialog()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }

                                    else{
                                        try {
                                            val errorResponse = Gson().fromJson(response.errorBody()?.charStream(), ResponseModel::class.java)
                                            Toast.makeText(this@EditProfileActivity, errorResponse.message, Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                                    Log.d("profile_backend", "onFailure: ${call.request().url()}")
                                    t.printStackTrace()
                                    DialogHelper.hideLoadingDialog()
                                }
                            })
                    }
                }
                else {

                    jsonObjectProfile.put("role", role)

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
                                    if (response.code() == 200) {

                                        val prefManager = PrefManager(this@EditProfileActivity)
                                        prefManager.profileRole = role
                                        val i = Intent(this@EditProfileActivity, HomeActivity::class.java)
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(i)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this@EditProfileActivity,
                                            response.body()?.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    DialogHelper.hideLoadingDialog()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            else{
                                try {
                                    val errorResponse = Gson().fromJson(response.errorBody()?.charStream(), ResponseModel::class.java)
                                    Toast.makeText(this@EditProfileActivity, errorResponse.message, Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                            Log.d("profile_backend", "onFailure: ${call.request().url()}")
                            t.printStackTrace()
                            DialogHelper.hideLoadingDialog()
                        }
                    })
                }
            }
        })
    }
}