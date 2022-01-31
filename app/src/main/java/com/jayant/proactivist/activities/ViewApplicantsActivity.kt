package com.jayant.proactivist.activities

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jayant.proactivist.R
import com.jayant.proactivist.adapters.ApplicationAdapter
import com.jayant.proactivist.models.Application
import com.jayant.proactivist.models.JobItemModel
import com.jayant.proactivist.models.Profile
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ViewApplicantsActivity : AppCompatActivity() {

    private var count: Long = 0
    private lateinit var rv_applicants: RecyclerView
    private lateinit var tv_app_bar: TextView
    private lateinit var iv_back: CircleImageView
    private val TAG = ViewApplicantsActivity::class.java.simpleName
    private lateinit var applicantsAdapter: ApplicationAdapter
    private var profileList = ArrayList<Profile>()
    private var dateList = ArrayList<String>()
    val formatter = SimpleDateFormat("dd-M-yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_applicants)

        rv_applicants = findViewById(R.id.rv_applicants)
        tv_app_bar = findViewById(R.id.tv_app_bar)
        iv_back = findViewById(R.id.iv_back)

        tv_app_bar.text = "All Applicants"
        iv_back.setOnClickListener {
            finish()
        }

        val job = intent?.getParcelableExtra<JobItemModel>("job")
        val database = FirebaseDatabase.getInstance()
        val job_poster = job?.job_poster
        val job_id = job?.job_id
        if (job_id != null && job_poster != null) {
            val query = database.reference.child("users").child(job_poster).child("jobs").child(job_id)
                query.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach {
                            val date = Date()
                            it.key?.toLong()?.let { it1 -> date.time = it1 }
                            dateList.add(formatter.format(date))
                            val id = it.getValue(String::class.java)
                            getProfileById(id)
                        }
                        count = snapshot.childrenCount
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "onCancelled: " + error.message)
                    }

                })
        }

        Log.d(TAG, "onCreate profileList: $profileList")

        rv_applicants.apply {
            applicantsAdapter = ApplicationAdapter(context, ArrayList<Application>(), supportFragmentManager)
            adapter = applicantsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

    }

    private fun getProfileById(id: String?){
        val database = FirebaseDatabase.getInstance()
        if (id != null) {
            database.reference.child("users").child(id).child("profile")
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val profile = snapshot.getValue(Profile::class.java)
                        if (profile != null) {
                            profileList.add(profile)
                        }
                        if(profileList.size.toLong() == count){
                            applicantsAdapter.notifyDataSetChanged()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }
    }
}