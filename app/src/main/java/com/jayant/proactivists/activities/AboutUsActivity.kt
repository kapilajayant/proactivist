package com.jayant.proactivists.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.jayant.proactivists.R
import com.jayant.proactivists.utils.Constants
import com.jayant.proactivists.utils.Constants.DISCORD
import com.jayant.proactivists.utils.Constants.INSTAGRAM
import com.jayant.proactivists.utils.Constants.LINKEDIN
import com.jayant.proactivists.utils.Constants.TWITTER
import com.jayant.proactivists.utils.Constants.WEBSITE

class AboutUsActivity : AppCompatActivity() {

    private lateinit var iv_back: ImageView
    private lateinit var iv_discord: ImageView
    private lateinit var iv_linkedin: ImageView
    private lateinit var iv_instagram: ImageView
    private lateinit var iv_twitter: ImageView
    private lateinit var tv_app_bar: TextView
    private lateinit var tv_link: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        tv_app_bar = findViewById(R.id.tv_app_bar)
        iv_back = findViewById(R.id.iv_back)
        tv_link = findViewById(R.id.tv_link)
        iv_discord = findViewById(R.id.iv_discord)
        iv_linkedin = findViewById(R.id.iv_linkedin)
        iv_instagram = findViewById(R.id.iv_instagram)
        iv_twitter = findViewById(R.id.iv_twitter)

        iv_back.setOnClickListener {
            finish()
        }

        tv_app_bar.text = "About us"

        tv_link.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE))
                startActivity(intent)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }

        iv_discord.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DISCORD))
                startActivity(intent)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }

        iv_linkedin.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(LINKEDIN))
                startActivity(intent)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }

        iv_instagram.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM))
                startActivity(intent)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }

        iv_twitter.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TWITTER))
                startActivity(intent)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
}