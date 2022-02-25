package com.jayant.proactivist.activities

import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import com.jayant.proactivist.R
import com.jayant.proactivist.utils.DialogHelper
import de.hdodenhof.circleimageview.CircleImageView
import androidx.browser.customtabs.CustomTabsIntent




class TopicActivity : AppCompatActivity() {

    private lateinit var tv_app_bar: TextView
    private lateinit var iv_back: ImageView
    private lateinit var webView: WebView
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic)

        tv_app_bar = findViewById(R.id.tv_app_bar)
        iv_back = findViewById(R.id.iv_back)
        webView = findViewById(R.id.web_view)

        tv_app_bar.text = intent.getStringExtra("title")
        iv_back.setOnClickListener {
            finish()
        }

        DialogHelper.showLoadingDialog(this@TopicActivity)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String) {
                DialogHelper.hideLoadingDialog()
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                DialogHelper.hideLoadingDialog()
            }

        }
        val url = intent.getStringExtra("url")
        if (url != null) {
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(this, Uri.parse(url))
//            webView.loadUrl(url)
        }

    }

    override fun onBackPressed() {
        if(webView.canGoBack()){
            webView.goBack()
        }
        else{
            finish()
        }
    }

}