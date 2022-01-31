package com.jayant.proactivist.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.jayant.proactivist.R
import com.jayant.proactivist.activities.ProfileActivity
import com.jayant.proactivist.activities.TopicActivity
import de.hdodenhof.circleimageview.CircleImageView

class LearnFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    private lateinit var iv_profile : ImageView
    private lateinit var tv_profile_name : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_learn, container, false)
        val card_android: CardView = view.findViewById(R.id.card_android)
        val card_react: CardView = view.findViewById(R.id.card_react)
        val card_python: CardView = view.findViewById(R.id.card_python)
        val card_ios: CardView = view.findViewById(R.id.card_ios)
        val card_php: CardView = view.findViewById(R.id.card_php)
        val card_django: CardView = view.findViewById(R.id.card_django)
        val card_angular: CardView = view.findViewById(R.id.card_angular)
        val card_vue: CardView = view.findViewById(R.id.card_vue)
        val fab_post: FloatingActionButton = view.findViewById(R.id.fab_post)

        tv_profile_name = view.findViewById(R.id.tv_profile_name)
        iv_profile = view.findViewById(R.id.iv_profile)

        setupProfile()

        fab_post.setOnClickListener {
            val newTopicPostFragment = NewTopicPostFragment()
            fragmentManager?.let { it1 -> newTopicPostFragment.show(it1, "") }
        }

        card_android.setOnClickListener { view1: View? ->
            val intent = Intent(context, TopicActivity::class.java)
            intent.putExtra("topic", "Android")
            intent.putExtra("url", "https://techbeacon.com/app-dev-testing/ultimate-android-development-guide-50-beginner-expert-resources")
            context?.startActivity(intent)
        }
        card_react.setOnClickListener { view1: View? ->
            val intent = Intent(context, TopicActivity::class.java)
            intent.putExtra("topic", "React")
            context?.startActivity(intent)
        }
        card_python.setOnClickListener { view1: View? ->
            val intent = Intent(context, TopicActivity::class.java)
            intent.putExtra("topic", "Python")
            context?.startActivity(intent)
        }
        card_ios.setOnClickListener { view1: View? ->
            val intent = Intent(context, TopicActivity::class.java)
            intent.putExtra("topic", "IOS")
            context?.startActivity(intent)
        }
        card_php.setOnClickListener { view1: View? ->
            val intent = Intent(context, TopicActivity::class.java)
            intent.putExtra("topic", "PHP")
            context?.startActivity(intent)
        }
        card_django.setOnClickListener { view1: View? ->
            val intent = Intent(context, TopicActivity::class.java)
            intent.putExtra("topic", "Django")
            context?.startActivity(intent)
        }
        card_angular.setOnClickListener { view1: View? ->
            val intent = Intent(context, TopicActivity::class.java)
            intent.putExtra("topic", "Angular")
            context?.startActivity(intent)
        }
        card_vue.setOnClickListener { view1: View? ->
            val intent = Intent(context, TopicActivity::class.java)
            intent.putExtra("topic", "Vue")
            context?.startActivity(intent)
        }
        return view
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(param1: String?, param2: String?): LearnFragment {
            val fragment = LearnFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    private fun setupProfile(){
        val currentUser = FirebaseAuth.getInstance().currentUser
        tv_profile_name.text = "Hi " + currentUser?.displayName
        context?.let { context ->
            Glide.with(context).load(currentUser?.photoUrl).apply(RequestOptions.circleCropTransform()).into(iv_profile)
            iv_profile.setOnClickListener {
                val intent = Intent(context, ProfileActivity::class.java)
                context.startActivity(intent)
            }
        }
    }

}