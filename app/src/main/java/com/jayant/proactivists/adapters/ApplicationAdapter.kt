package com.jayant.proactivists.adapters

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.jayant.proactivists.activities.ChatActivity
import com.jayant.proactivists.R
import com.jayant.proactivists.activities.CandidateDetailActivity
import com.jayant.proactivists.fragments.HelpFragment
import com.jayant.proactivists.fragments.NoInternetFragment
import com.jayant.proactivists.fragments.StatusFragment
import com.jayant.proactivists.models.Application
import com.jayant.proactivists.models.Profile
import com.jayant.proactivists.utils.Constants
import com.jayant.proactivists.utils.Constants.ACCEPTED
import com.jayant.proactivists.utils.Constants.COMPLETED
import com.jayant.proactivists.utils.Constants.PENDING
import com.jayant.proactivists.utils.Constants.REJECTED
import com.jayant.proactivists.utils.Constants.SUBMITTED
import com.jayant.proactivists.utils.NetworkManager
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ApplicationAdapter(var context: Context, var applicationsList: ArrayList<Application>, var fragmentManager: FragmentManager): RecyclerView.Adapter<JobsViewHolder>() {

    private val TAG = ApplicationAdapter::class.java.simpleName
    private lateinit var reference: Query
    private lateinit var listener: ValueEventListener
    private var showCandidates = false
    val formatter = SimpleDateFormat("dd-M-yyyy")

    constructor(context: Context, applicationsList: ArrayList<Application>, showCandidates: Boolean, fragmentManager: FragmentManager) : this(context, applicationsList, fragmentManager) {
        this.showCandidates = showCandidates
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobsViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.card_layout_profile, parent, false)
        return JobsViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobsViewHolder, position: Int) {
        try {
            when(applicationsList[position].status){
                REJECTED ->{
                    holder.ivAction.visibility = View.GONE
                    holder.constraintParent.background = ColorDrawable(ContextCompat.getColor(context, R.color.red_tint))
//                    holder.ivAction.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_reject))
                }
                PENDING ->{
                    holder.ivAction.visibility = View.GONE
                    holder.constraintParent.background = ColorDrawable(ContextCompat.getColor(context, R.color.yellow_tint))
//                    holder.ivAction.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pending))
                }
                ACCEPTED ->{
                    holder.constraintParent.background = ColorDrawable(ContextCompat.getColor(context, R.color.green_tint))
//                    holder.ivAction.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_accept))
                }
                SUBMITTED ->{
                    holder.constraintParent.background = ColorDrawable(ContextCompat.getColor(context, R.color.purple_200))
//                    holder.ivAction.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_accept))
                }
                COMPLETED ->{
                    holder.constraintParent.background = ColorDrawable(ContextCompat.getColor(context, R.color.blue))
//                    holder.ivAction.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_accept))
                }
            }

            if(showCandidates){
                setCandidateProfile(applicationsList[position], holder = holder)
            }
            else {
                setReferrerProfile(applicationsList[position], holder = holder)
            }

        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setReferrerProfile(application: Application, holder: JobsViewHolder) {
        holder.tvTitle.text = application.company_name
        holder.tvDesc.text = application.timestamp?.substring(0, 10)
        holder.linearReferrer.visibility = View.VISIBLE
        Glide.with(context).load(application.company_logo).into(holder.ivLogo)

        holder.tvReferrer.text = application.name
        Glide.with(context).load(application.photo).into(holder.ivProfile)

        holder.ivAction.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            val profile = Profile()
            profile.uid = application.uid
            profile.name = application.name
            profile.photo = application.photo
            profile.role = Constants.REFERRER
            intent.putExtra(Constants.RECEIVER_PROFILE, profile)
            context.startActivity(intent)
        }

        holder.cardProfile.setOnClickListener {
            val fragment = application.status?.let { it1 -> StatusFragment(it1) }
            fragment?.show(fragmentManager, "")
        }
    }

    private fun setCandidateProfile(application: Application, holder: JobsViewHolder) {
        holder.tvTitle.text = application.name

        holder.tvDesc.text = application.timestamp?.substring(0, 10)
        Glide.with(context).load(application.photo).into(holder.ivLogo)

        holder.ivAction.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            val profile = Profile()
            profile.uid = application.uid
            profile.name = application.name
            profile.photo = application.photo
            profile.role = Constants.CANDIDATE
            intent.putExtra(Constants.RECEIVER_PROFILE, profile)
            intent.putExtra("status", application.status)
            context.startActivity(intent)
        }

        holder.cardProfile.setOnClickListener {
            if(NetworkManager.getConnectivityStatusString(context) != Constants.NO_INTERNET) {

                val intent = Intent(context, CandidateDetailActivity::class.java)
                intent.putExtra("status", application.status)
                intent.putExtra("profileId", application.uid)
                intent.putExtra("application_id", application.application_id)
                context.startActivity(intent)
            }
            else{
                val fragment = NoInternetFragment()
                fragment.show(fragmentManager, "")
            }
        }
    }

    override fun getItemCount(): Int = applicationsList.size

}

class JobsViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
    val tv_profile = itemView.findViewById<TextView>(R.id.tv_profile)
    val tvTitle = itemView.findViewById<TextView>(R.id.tv_title)
    val tvDesc = itemView.findViewById<TextView>(R.id.tv_desc)
    val tvReferrer = itemView.findViewById<TextView>(R.id.tv_referrer)
    val linearReferrer = itemView.findViewById<LinearLayout>(R.id.linear_referrer)
    val ivLogo = itemView.findViewById<CircleImageView>(R.id.iv_logo)
    val ivProfile = itemView.findViewById<ImageView>(R.id.iv_profile)
    val ivAction = itemView.findViewById<ImageView>(R.id.iv_action)
    val cardProfile = itemView.findViewById<CardView>(R.id.card_profile)
    val constraintParent = itemView.findViewById<ConstraintLayout>(R.id.constraintParent)
}