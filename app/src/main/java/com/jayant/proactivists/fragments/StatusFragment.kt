package com.jayant.proactivists.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jayant.proactivists.R
import com.jayant.proactivists.rest.APIService
import com.jayant.proactivists.rest.ApiUtils
import com.jayant.proactivists.utils.Constants
import com.jayant.proactivists.utils.Constants.APP_URL

class StatusFragment(var status: Int) : SuperBottomSheetFragment() {

    private lateinit var tv_title: TextView
    private lateinit var tv_desc: TextView
    private lateinit var btn_update: TextView
    private lateinit var card: CardView
    lateinit var apiService: APIService
    var color = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        super.onCreateView(inflater, container, savedInstanceState)

        apiService = ApiUtils.getAPIService()

        val view = inflater.inflate(R.layout.layout_status, container, false)

        tv_title = view.findViewById(R.id.tv_title)
        tv_desc = view.findViewById(R.id.tv_desc)
        btn_update = view.findViewById(R.id.btn_update)
        card = view.findViewById(R.id.card)

        when(status){
            Constants.REJECTED ->{
                tv_title.text = "Rejected"
                tv_desc.text = "Your profile didn't seem to be a fit for the role to the referrer."
                color = ContextCompat.getColor(requireContext(), R.color.red)
            }
            Constants.ACCEPTED ->{
                tv_title.text = "Accepted"
                tv_desc.text = "Your profile has been accepted. Referrer will submit your application and we will verify it soon."
                color = ContextCompat.getColor(requireContext(), R.color.green)
            }
            Constants.PENDING ->{
                card.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black))
                tv_title.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tv_desc.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tv_title.text = "Pending"
                tv_desc.text = "Referrer is yet to decide about your application. Check again in some time."
                color = ContextCompat.getColor(requireContext(), R.color.yellow)
            }
            Constants.SUBMITTED ->{
                tv_title.text = "Submitted"
                tv_desc.text = "Referrer has submitted your profile and we are confirming that from our end."
                color = ContextCompat.getColor(requireContext(), R.color.purple_200)
            }
            Constants.COMPLETED ->{
                tv_title.text = "Completed"
                tv_desc.text = "Your referral is complete and we have verified from our end."
                color = ContextCompat.getColor(requireContext(), R.color.blue)
            }
            Constants.UPDATE ->{
                isCancelable = false
                color = ContextCompat.getColor(requireContext(), R.color.purple_500)
                btn_update.visibility = View.VISIBLE
                tv_title.text = "Update available"
                tv_desc.text = "A new update is available. Update the app now from play store."
            }
        }

        btn_update.setOnClickListener {
            val uri = Uri.parse(APP_URL)
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            startActivity(goToMarket)
        }

        return view
    }

    override fun getBackgroundColor(): Int {
        return color
    }

    override fun getStatusBarColor(): Int {
        return ContextCompat.getColor(requireContext(), R.color.white)
    }

    override fun animateStatusBar(): Boolean {
        return true
    }

}