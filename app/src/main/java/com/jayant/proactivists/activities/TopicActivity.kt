package com.jayant.proactivists.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import com.jayant.proactivists.R
import de.hdodenhof.circleimageview.CircleImageView

class TopicActivity : AppCompatActivity() {

    private lateinit var tv_app_bar: TextView
    private lateinit var iv_back: ImageView
    private lateinit var webView: WebView
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("")
        progressDialog.setMessage("Loading...")
        progressDialog.show()

        tv_app_bar = findViewById(R.id.tv_app_bar)
        iv_back = findViewById(R.id.iv_back)
        webView = findViewById(R.id.web_view)

        tv_app_bar.text = intent.getStringExtra("topic")
        iv_back.setOnClickListener {
            finish()
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String) {
                if(progressDialog.isShowing){
                    progressDialog.dismiss()
                }
            }
        }
        val url = intent.getStringExtra("url")
        if (url != null) {
            webView.loadUrl(url)
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