package com.jayant.proactivist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.jayant.proactivist.R;
import com.jayant.proactivist.models.CompanySuggestion;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CompanySearchAdapter extends ArrayAdapter<CompanySuggestion> {

    private ArrayList<CompanySuggestion> suggestions;
    private Context context;
    private int resource;
    private CompanySelected companySelected;

    public CompanySearchAdapter(Context context, int resource, ArrayList<CompanySuggestion> suggestions, CompanySelected companySelected) {
        super(context, resource, suggestions);
        this.suggestions = suggestions;
        this.context = context;
        this.resource = resource;
        this.companySelected = companySelected;
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        try {

            if (view == null) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
            }

            TextView tv_job_title = (TextView) view.findViewById(R.id.tv_job_title);
            TextView tv_company_name = (TextView) view.findViewById(R.id.tv_company_name);
            CircleImageView iv_logo = (CircleImageView) view.findViewById(R.id.iv_logo);
            ImageView iv_save = (ImageView) view.findViewById(R.id.iv_save);
            CardView card_job = (CardView) view.findViewById(R.id.card_job);
            tv_job_title.setVisibility(View.GONE);
            iv_save.setVisibility(View.GONE);
            tv_company_name.setText(suggestions.get(position).getName());
            Glide.with(context).load(suggestions.get(position).getLogo()).into(iv_logo);
            card_job.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    companySelected.companySelected(suggestions.get(position));
//                    Toast.makeText(context, "Opening " + suggestions.get(position).getName(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

}