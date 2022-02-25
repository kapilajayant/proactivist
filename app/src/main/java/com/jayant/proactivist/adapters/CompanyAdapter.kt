package com.jayant.proactivist.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jayant.proactivist.R
import com.jayant.proactivist.models.CompanySuggestion

class CompanyAdapter(var context: Context, var suggestions: ArrayList<CompanySuggestion>, val companySelected: CompanySelected): RecyclerView.Adapter<CompanyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.card_layout_job, parent, false)
        return CompanyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
        try {

            holder.tv_job_title.visibility = View.GONE
            holder.iv_save.visibility = View.GONE
            holder.tv_company_name.text = suggestions[position].name
            Glide.with(context).load(suggestions[position].logo).into(holder.iv_logo)
            holder.card_job.setOnClickListener {
                companySelected.companySelected(suggestions[position])
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = suggestions.size

}

public class CompanyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tv_job_title = view.findViewById<TextView>(R.id.tv_job_title)
    val tv_company_name = view.findViewById<TextView>(R.id.tv_company_name)
    val iv_save = view.findViewById<ImageView>(R.id.iv_save)
    val iv_logo = view.findViewById<ImageView>(R.id.iv_logo)
    val card_job = view.findViewById<CardView>(R.id.card_job)
}