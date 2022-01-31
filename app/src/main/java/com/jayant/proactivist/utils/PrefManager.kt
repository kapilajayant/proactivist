package com.jayant.proactivist.utils

import android.content.Context
import android.content.SharedPreferences
import com.jayant.proactivist.BuildConfig
import com.jayant.proactivist.models.Profile

class PrefManager(var _context: Context) {

    var pref: SharedPreferences
    var editor: SharedPreferences.Editor

    private var PRIVATE_MODE = 0

    companion object {
        private const val PREF_PROFILE = "pref_profile"
        private const val PROFILE_NAME = "profile_name"
        private const val PROFILE_PHONE = "profile_phone"
        private const val PROFILE_EMAIL = "profile_email"
        private const val PROFILE_EXPERIENCE = "profile_experience"
        private const val PROFILE_LINKEDIN_PERSONAL = "profile_linkedin_personal"
        private const val PROFILE_LINKEDIN_COMPANY = "profile_linkedin_company"
        private const val PROFILE_COMPANY_NAME = "profile_company_name"
        private const val PROFILE_SKILLS = "profile_skills"
        private const val PROFILE_SUMMARY = "profile_summary"
        private const val PROFILE_COMPANY_LOGO = "profile_company_logo"
        private const val PROFILE_ROLE = "profile_role"
        private const val PROFILE_PHOTO = "profile_photo"
        private const val PROFILE_RESUME = "profile_resume"
        private const val COMP_ID = "comp_id"
        private const val TOKEN = "token"
        private const val LATEST_VERSION_CODE = "latest_version_code"
    }

    init {
        pref = _context.getSharedPreferences(PREF_PROFILE, PRIVATE_MODE)
        editor = pref.edit()
    }

    var profileName: String?
        get() = pref.getString(PROFILE_NAME, "")
        set(profileName) {
            editor.putString(PROFILE_NAME, profileName)
            editor.apply()
        }

    var profilePhone: String?
        get() = pref.getString(PROFILE_PHONE, "")
        set(profileName) {
            editor.putString(PROFILE_PHONE, profileName)
            editor.apply()
        }

    var profileEmail: String?
        get() = pref.getString(PROFILE_EMAIL, "")
        set(profileEmail) {
            editor.putString(PROFILE_EMAIL, profileEmail)
            editor.apply()
        }

    var profileExperience: String?
        get() = pref.getString(PROFILE_EXPERIENCE, "")
        set(profileExperience) {
            editor.putString(PROFILE_EXPERIENCE, profileExperience)
            editor.apply()
        }

    var profileLinkedinPersonal: String?
        get() = pref.getString(PROFILE_LINKEDIN_PERSONAL, "")
        set(profileLinkedinPersonal) {
            editor.putString(PROFILE_LINKEDIN_PERSONAL, profileLinkedinPersonal)
            editor.apply()
        }

    var profileLinkedinCompany: String?
        get() = pref.getString(PROFILE_LINKEDIN_COMPANY, "")
        set(profileLinkedinCompany) {
            editor.putString(PROFILE_LINKEDIN_COMPANY, profileLinkedinCompany)
            editor.apply()
        }

    var profileCompanyName: String?
        get() = pref.getString(PROFILE_COMPANY_NAME, "")
        set(profileCompanyName) {
            editor.putString(PROFILE_COMPANY_NAME, profileCompanyName)
            editor.apply()
        }

    var profileSkills: String?
        get() = pref.getString(PROFILE_SKILLS, "")
        set(profileSkills) {
            editor.putString(PROFILE_SKILLS, profileSkills)
            editor.apply()
        }

    var profileSummary: String?
        get() = pref.getString(PROFILE_SUMMARY, "")
        set(profileSummary) {
            editor.putString(PROFILE_SUMMARY, profileSummary)
            editor.apply()
        }

    var profileCompanyLogo: String?
        get() = pref.getString(PROFILE_COMPANY_LOGO, "")
        set(profileCompanyLogo) {
            editor.putString(PROFILE_COMPANY_LOGO, profileCompanyLogo)
            editor.apply()
        }

    var profileRole: String?
        get() = pref.getString(PROFILE_ROLE, "")
        set(profileRole) {
            editor.putString(PROFILE_ROLE, profileRole)
            editor.apply()
        }

    var profilePhoto: String?
        get() = pref.getString(PROFILE_PHOTO, "")
        set(profilePhoto) {
            editor.putString(PROFILE_PHOTO, profilePhoto)
            editor.apply()
        }

    var profileResume: String?
        get() = pref.getString(PROFILE_RESUME, "")
        set(profileResume) {
            editor.putString(PROFILE_RESUME, profileResume)
            editor.apply()
        }

    var compId: String?
        get() = pref.getString(COMP_ID, "")
        set(compId) {
            editor.putString(COMP_ID, compId)
            editor.apply()
        }

    var token: String?
        get() = pref.getString(TOKEN, "")
        set(token) {
            editor.putString(TOKEN, token)
            editor.apply()
        }

    var latestVersionCode: Int
        get() = pref.getInt(LATEST_VERSION_CODE, BuildConfig.VERSION_CODE)
        set(latestVersionCode) {
            editor.putInt(LATEST_VERSION_CODE, latestVersionCode)
            editor.apply()
        }

    fun setProfile(profile: Profile){
        this.profileName = profile.name
        this.profilePhone = profile.phone
        this.profileEmail = profile.email
        this.profileExperience = profile.experience
        this.profileCompanyName = profile.company_name
        this.profileCompanyLogo = profile.company_logo
        this.profileSummary = profile.about
        this.profileSkills = profile.skills
        this.profileLinkedinPersonal = profile.personal_linkedin
        this.profileLinkedinCompany = profile.company_linkedin
        this.profileRole = profile.role
        this.profilePhoto = profile.photo
        this.profileResume = profile.resume
        this.compId = profile.comp_id
    }
}