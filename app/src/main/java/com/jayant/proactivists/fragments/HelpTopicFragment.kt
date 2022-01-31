package com.jayant.proactivists.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jayant.proactivists.R
import com.jayant.proactivists.rest.APIService
import com.jayant.proactivists.rest.ApiUtils
import com.jayant.proactivists.utils.Constants.HELP_COINS
import com.jayant.proactivists.utils.Constants.HELP_HOW_IT_WORKS
import com.jayant.proactivists.utils.Constants.HELP_MY_ACCOUNT
import com.jayant.proactivists.utils.Constants.HELP_STATUS

class HelpTopicFragment(var type: Int) : BottomSheetDialogFragment() {

    private lateinit var tv_title: TextView
    private lateinit var tv_desc: TextView
    lateinit var apiService: APIService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        apiService = ApiUtils.getAPIService()

        val view = inflater.inflate(R.layout.layout_help_topic, container, false)

        tv_title = view.findViewById(R.id.tv_title)
        tv_desc = view.findViewById(R.id.tv_desc)

        when(type){
            HELP_HOW_IT_WORKS ->{
                tv_title.text = "how it works?"
                tv_desc.text =  resources.getString(R.string.help_how_it_works)
            }
            HELP_COINS ->{
                tv_title.text = "coins"
                tv_desc.text =  resources.getString(R.string.help_coins)
            }
            HELP_MY_ACCOUNT ->{
                tv_title.text = "my account"
                tv_desc.text =  resources.getString(R.string.help_my_account)
            }
            HELP_STATUS ->{
                tv_title.text = "status"
                tv_desc.text =  resources.getString(R.string.help_status)
            }
        }

        return view
    }

}