package com.jayant.proactivist.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.jayant.proactivist.R
import com.jayant.proactivist.adapters.ArticleAdapter
import com.jayant.proactivist.models.ListResponseModel
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.models.learn.ArticleModel
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.utils.DialogHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class ArticleListActivity : AppCompatActivity() {

    private lateinit var tv_app_bar: TextView
    private lateinit var iv_back: ImageView
    private lateinit var rv_article : RecyclerView
    private var articleAdapter: ArticleAdapter? = null
    private var articleList = ArrayList<ArticleModel>()
    lateinit var apiService: APIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_list)

        apiService = ApiUtils.getAPIService()

        tv_app_bar = findViewById(R.id.tv_app_bar)
        iv_back = findViewById(R.id.iv_back)
        rv_article = findViewById(R.id.rv_article)

        val topic = intent.getStringExtra("topic")
        val image = intent.getStringExtra("image")

        tv_app_bar.text = topic
        iv_back.setOnClickListener {
            finish()
        }

//        articleList.add(ArticleModel("Introduction to Android Development", "https://www.geeksforgeeks.org/introduction-to-android-development/"))
//        articleList.add(ArticleModel("Android Architecture", "https://www.geeksforgeeks.org/android-architecture/?ref=lbp"))
//        articleList.add(ArticleModel("Android UI Layouts", "https://www.geeksforgeeks.org/android-ui-layouts/?ref=lbp"))
//        articleList.add(ArticleModel("", ""))

        if (topic != null) {
            getArticles(topic)
        }

        articleAdapter = image?.let { ArticleAdapter(this, articleList, it, supportFragmentManager) }

        rv_article.apply {
            layoutManager = LinearLayoutManager(this@ArticleListActivity, LinearLayoutManager.VERTICAL, false)
            adapter = articleAdapter
        }

    }

    private fun getArticles(topic: String) {
        DialogHelper.showLoadingDialog(this)

        apiService.getArticles(topic).enqueue(object : Callback<ListResponseModel> {
            override fun onResponse(
                call: Call<ListResponseModel>,
                response: Response<ListResponseModel>
            ) {
                DialogHelper.hideLoadingDialog()
                if (response.isSuccessful) {
                    try {
                        if(response.code() == 200){
                            response.body()?.getArticlesResponse()?.forEach {
                                articleList.add(it)
                            }
                            articleAdapter?.notifyDataSetChanged()
                        }
                        else {
                            Toast.makeText(this@ArticleListActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@ArticleListActivity, "Something went wrong!", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
                else {
                    try {
                        val errorResponse = Gson().fromJson(
                            response.errorBody()?.charStream(),
                            ResponseModel::class.java
                        )
                        Toast.makeText(
                            this@ArticleListActivity,
                            errorResponse.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<ListResponseModel>, t: Throwable) {
                DialogHelper.hideLoadingDialog()
                Toast.makeText(this@ArticleListActivity, "Something went wrong!", Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }
        })
    }
}