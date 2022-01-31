package com.jayant.proactivist

import android.content.Context
import androidx.fragment.app.FragmentPagerAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.jayant.proactivist.R
import com.jayant.proactivist.fragments.TutorialFragment

class SliderAdapter(fm: FragmentManager, var context: Context) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        val bundle = Bundle()
        bundle.putInt("position", position)
        when (position) {
            0 -> {
                bundle.putString("title", context.resources.getString(R.string.first_slide_title))
                bundle.putString("desc", context.resources.getString(R.string.first_slide_desc))
                val tutorialFragment = TutorialFragment()
                tutorialFragment.arguments = bundle
                return tutorialFragment
            }
            1 -> {
                bundle.putString("title", context.resources.getString(R.string.second_slide_title))
                bundle.putString("desc", context.resources.getString(R.string.second_slide_desc))
                val tutorialFragment = TutorialFragment()
                tutorialFragment.arguments = bundle
                return tutorialFragment
            }
            2 -> {
                bundle.putString("title", context.resources.getString(R.string.third_slide_title))
                bundle.putString("desc", context.resources.getString(R.string.third_slide_desc))
                val tutorialFragment = TutorialFragment()
                tutorialFragment.arguments = bundle
                return tutorialFragment
            }
        }
        bundle.putString("title", context.resources.getString(R.string.first_slide_title))
        bundle.putString("desc", context.resources.getString(R.string.first_slide_desc))
        val tutorialFragment = TutorialFragment()
        tutorialFragment.arguments = bundle
        return tutorialFragment
    }

    override fun getCount(): Int {
        return 3
    }
}