package com.jayant.proactivist.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.jayant.proactivist.R
import com.jayant.proactivist.activities.ProfileActivity
import com.jayant.proactivist.adapters.ShopAdapter
import com.jayant.proactivist.models.ShopModel
import java.util.ArrayList

class ShopFragment : Fragment() {

    private lateinit var rv_shop : RecyclerView
    private lateinit var shopAdapter: ShopAdapter
    private var list = ArrayList<ShopModel>()
    private lateinit var tv_profile_name : TextView
    private lateinit var tv_coins : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        
        val view = inflater.inflate(R.layout.fragment_shop, container, false)

        rv_shop = view.findViewById(R.id.rv_shop)
        tv_profile_name = view.findViewById(R.id.tv_profile_name)
        tv_coins = view.findViewById(R.id.tv_coins)

        setupProfile()

        shopAdapter = ShopAdapter(requireContext(), list)

        list.add(ShopModel("boAt Rockerz 510 Super Extra Bass Bluetooth Headset", R.drawable.boat_rocker, "8000"))
        list.add(ShopModel("Mivi DuoPods M20 True Wireless Bluetooth Headset", R.drawable.mivi_earbus, "7500"))
        list.add(ShopModel("Mivi Play 5 W Portable Bluetooth Speaker", R.drawable.mivi_speaker, "8000"))
        list.add(ShopModel("Google Home Mini with Google Assistant Smart Speaker", R.drawable.google_home_mini, "10000"))
        list.add(ShopModel("Echo Dot (3rd Gen)", R.drawable.amazon_echo_dot, "10000"))

        rv_shop.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = shopAdapter
        }

        shopAdapter.notifyDataSetChanged()

        return view
    }

    private fun setupProfile(){
        val currentUser = FirebaseAuth.getInstance().currentUser
        tv_profile_name.text = "Hi " + currentUser?.displayName?.substringBefore(" ")
        tv_profile_name.text = "Shop"
        tv_coins.text = "10000"
        context?.let { context ->
            tv_coins.setOnClickListener {
                val intent = Intent(context, ProfileActivity::class.java)
                context.startActivity(intent)
            }
        }
    }
}