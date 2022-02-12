package com.jayant.proactivist.activities

import android.R.id
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jayant.proactivist.R
import com.jayant.proactivist.fragments.HelpTopicFragment
import com.jayant.proactivist.utils.Constants
import android.R.id.message
import java.net.URLEncoder


class HelpActivity : AppCompatActivity() {

    private lateinit var tv_app_bar: TextView
    private lateinit var tv_help_how_it_works: TextView
    private lateinit var tv_help_coins: TextView
    private lateinit var tv_help_my_account: TextView
    private lateinit var tv_help_status: TextView
    private lateinit var iv_back: ImageView
    private lateinit var iv_whatsapp: ImageView
    private lateinit var iv_mail: ImageView
    private lateinit var iv_call: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        tv_app_bar = findViewById(R.id.tv_app_bar)
        iv_back = findViewById(R.id.iv_back)
        iv_whatsapp = findViewById(R.id.iv_whatsapp)
        iv_mail = findViewById(R.id.iv_mail)
        iv_call = findViewById(R.id.iv_call)
        tv_help_how_it_works = findViewById(R.id.tv_help_how_it_works)
        tv_help_coins = findViewById(R.id.tv_help_coins)
        tv_help_my_account = findViewById(R.id.tv_help_my_account)
        tv_help_status = findViewById(R.id.tv_help_status)

        tv_app_bar.text = "Help"
        iv_back.setOnClickListener {
            finish()
        }

        iv_call.setOnClickListener {
            try {
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:+918295019197")
                startActivity(callIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        iv_mail.setOnClickListener {

            val i = Intent(Intent.ACTION_SENDTO)
            i.type = "message/rfc822"
            i.data = Uri.parse("mailto:jayant@proactvist.in")
            i.putExtra(Intent.EXTRA_SUBJECT, "Query mail")
            i.putExtra(
                Intent.EXTRA_TEXT,
                "Hi I have some queries regarding your app and would like to get some help, Thanks"
            )
            try {
                startActivity(Intent.createChooser(i, "Send mail..."))
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    this@HelpActivity,
                    "There are no email clients installed.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        iv_whatsapp.setOnClickListener {
            if(whatsappInstalledOrNot("com.whatsapp")){
                shareWhatsapp("Hi I have some query regarding the app...")
            }
        }

        tv_help_how_it_works.setOnClickListener {
            val fragment = HelpTopicFragment(Constants.HELP_HOW_IT_WORKS)
            fragment.show(supportFragmentManager, "")
        }

        tv_help_coins.setOnClickListener {
            val fragment = HelpTopicFragment(Constants.HELP_COINS)
            fragment.show(supportFragmentManager, "")
        }

        tv_help_my_account.setOnClickListener {
            val fragment = HelpTopicFragment(Constants.HELP_MY_ACCOUNT)
            fragment.show(supportFragmentManager, "")
        }

        tv_help_status.setOnClickListener {
            val fragment = HelpTopicFragment(Constants.HELP_STATUS)
            fragment.show(supportFragmentManager, "")
        }

    }

    private fun shareWhatsapp(message: String) {
//        val sendIntent = Intent()
//        sendIntent.action = Intent.ACTION_SEND
//        sendIntent.putExtra(Intent.EXTRA_TEXT, message)
//        sendIntent.type = "text/plain"
//        sendIntent.setPackage("com.whatsapp")
//        sendIntent.component = ComponentName("com.whatsapp", "com.whatsapp.Conversation")
////        sendIntent.component = ComponentName("com.whatsapp", "com.whatsapp.ContactPicker")
//        intent.putExtra("jid", PhoneNumberUtils.stripSeparators("8295019197") + "@s.whatsapp.net")
//        if (sendIntent.resolveActivity(packageManager) != null) {
//            startActivity(sendIntent)
//        }
//
        val packageManager: PackageManager = packageManager
        val i = Intent(Intent.ACTION_VIEW)

        try {
            val url =
                "https://api.whatsapp.com/send?phone=+918295019197" + "&text=" + URLEncoder.encode(
                    message,
                    "UTF-8"
                )
            i.setPackage("com.whatsapp")
            i.data = Uri.parse(url)
            if (i.resolveActivity(packageManager) != null) {
                startActivity(i)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun whatsappInstalledOrNot(uri: String): Boolean {
        val pm = packageManager
        var app_installed = false
        app_installed = try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return app_installed
    }

}