package com.jayant.proactivist.fragments

import android.graphics.drawable.Drawable
import android.widget.TextView
import com.jayant.proactivist.R
import android.os.Bundle
import android.util.Log
import com.jayant.proactivist.fragments.TutorialFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

/**
 * A simple [Fragment] subclass.
 * Use the [TutorialFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TutorialFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var str_title: String? = null
    private var str_desc: String? = null
    private var position = 0
    private var img_fingerprint: Drawable? = null
    private var slider_image: ImageView? = null
    private var slider_heading: TextView? = null
    private var slider_desc: TextView? = null
    private val title_list = intArrayOf(
        R.string.first_slide_title,
        R.string.second_slide_title,
        R.string.third_slide_title
    )
    private val desc_list =
        intArrayOf(R.string.first_slide_desc, R.string.second_slide_desc, R.string.third_slide_desc)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            val bundle = arguments
            str_title = bundle!!.getString(ARG_PARAM1)
            str_desc = bundle.getString(ARG_PARAM2)
            position = bundle.getInt(ARG_PARAM3)
            Log.d(TAG, "onCreate str_title: $str_title")
            Log.d(TAG, "onCreate str_desc: $str_desc")
            Log.d(TAG, "onCreate position: $position")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tutorial, container, false)
        slider_heading = view.findViewById(R.id.slider_heading)
        slider_desc = view.findViewById(R.id.slider_desc)
        slider_image = view.findViewById(R.id.slider_image)
        slider_heading?.setText(resources.getString(title_list[position]))
        slider_desc?.setText(resources.getString(desc_list[position]))
        if (position == 0) {
            img_fingerprint = resources.getDrawable(R.drawable.illustration_connect)
        } else if (position == 1) {
            img_fingerprint = resources.getDrawable(R.drawable.illustration_refer)
        } else if (position == 2) {
            img_fingerprint = resources.getDrawable(R.drawable.illustration_learn)
        }
        slider_image?.setImageDrawable(img_fingerprint)
        return view
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "title"
        private const val ARG_PARAM2 = "desc"
        private const val ARG_PARAM3 = "position"
        private const val TAG = "Tutorial"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TutorialFivth.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): TutorialFragment {
            val fragment = TutorialFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            args.putString(ARG_PARAM3, param2)
            fragment.arguments = args
            return fragment
        }
    }
}