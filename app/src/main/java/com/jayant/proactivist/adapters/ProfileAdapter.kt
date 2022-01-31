package com.jayant.proactivist.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jayant.proactivist.R
import com.jayant.proactivist.activities.CandidateDetailActivity
import com.jayant.proactivist.activities.ReferrerProfileActivity
import com.jayant.proactivist.fragments.NoInternetFragment
import com.jayant.proactivist.models.Profile
import com.jayant.proactivist.models.get_candidates.GetCandidatesItem
import com.jayant.proactivist.models.get_referrers.GetReferrersItem
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.Constants.SHOW_CANDIDATES_LIST
import com.jayant.proactivist.utils.Constants.SHOW_COMPANIES_LIST
import com.jayant.proactivist.utils.NetworkManager
import com.jayant.proactivist.utils.ProfilePhotoLoader
import de.hdodenhof.circleimageview.CircleImageView

class ProfileAdapter(var context: Context, var uiType: Int, var fragmentManager: FragmentManager): RecyclerView.Adapter<ProfileViewHolder>() {

    private var candidatesList: ArrayList<GetCandidatesItem> = ArrayList()
    private var referrersList: ArrayList<GetReferrersItem> = ArrayList()

    fun setCandidatesList(candidatesList: ArrayList<GetCandidatesItem>){
        this.candidatesList = candidatesList
        notifyItemRangeInserted(0, candidatesList.size)
    }

    fun setReferrersList(referrersList: ArrayList<GetReferrersItem>){
        this.referrersList = referrersList
        notifyItemRangeInserted(0, referrersList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.card_layout_profile, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        try {
            holder.ivAction.visibility = View.GONE
//            holder.ivAction.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_next))

            when(uiType){
                SHOW_COMPANIES_LIST ->{
                    holder.tvTitle.text = referrersList[position].company_name
                    holder.tvDesc.text = referrersList[position].referrer_name
                    Glide.with(context).load(referrersList[position].company_logo).placeholder(ContextCompat.getDrawable(context, R.drawable.ic_business)).into(holder.ivLogo)

                    holder.cardProfile.setOnClickListener {
                        val intent = Intent(context, ReferrerProfileActivity::class.java)
                        intent.putExtra("referrer", referrersList[position])
                        context.startActivity(intent)
                    }
                }
                SHOW_CANDIDATES_LIST ->{
                    holder.tvTitle.text = candidatesList[position].can_name
                    holder.tvDesc.text = candidatesList[position].timestamp.substring(0, 10)
                    Glide.with(context).load(candidatesList[position].can_photo).placeholder(ContextCompat.getDrawable(context, R.drawable.ic_profile)).into(holder.ivLogo)
                    holder.cardProfile.setOnClickListener {
                        if(NetworkManager.getConnectivityStatusString(context) != Constants.NO_INTERNET) {
                            val intent = Intent(context, CandidateDetailActivity::class.java)
                            intent.putExtra("candidate", candidatesList[position])
                            context.startActivity(intent)
                        }
                        else{
                            val fragment = NoInternetFragment()
                            fragment.show(fragmentManager, "")
                        }
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*
    Passionate developer. Have built projects ranging from food delivery app to social media app. Love to help and guide others.
    Have helped more than 20 people in their job hunt, development journey etc.

     */

    override fun getItemCount(): Int {
        return if(!referrersList.isNullOrEmpty()){
            referrersList.size
        } else if(!candidatesList.isNullOrEmpty()){
            candidatesList.size
        } else{
            0
        }
    }
}


class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tv_profile = itemView.findViewById<TextView>(R.id.tv_profile)
    val tvTitle = itemView.findViewById<TextView>(R.id.tv_title)
    val ivLogo = itemView.findViewById<CircleImageView>(R.id.iv_logo)
    val tvDesc = itemView.findViewById<TextView>(R.id.tv_desc)
    val tvReferrer = itemView.findViewById<TextView>(R.id.tv_referrer)
    val linearReferrer = itemView.findViewById<LinearLayout>(R.id.linear_referrer)
    val ivProfile = itemView.findViewById<ImageView>(R.id.iv_profile)
    val ivAction = itemView.findViewById<ImageView>(R.id.iv_action)
    val cardProfile = itemView.findViewById<CardView>(R.id.card_profile)
}