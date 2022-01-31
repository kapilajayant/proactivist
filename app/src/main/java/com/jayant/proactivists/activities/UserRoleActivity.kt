package com.jayant.proactivists.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.jayant.proactivists.R
import com.jayant.proactivists.fragments.EditProfileFragment
import com.jayant.proactivists.fragments.InviteCodeFragment
import com.jayant.proactivists.utils.Constants

class UserRoleActivity : AppCompatActivity() {

    private lateinit var tv_enter_code: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_role)

        tv_enter_code = findViewById(R.id.tv_enter_code)
        val card_referrer = findViewById<CardView>(R.id.card_referrer)
        val card_candidate = findViewById<CardView>(R.id.card_candidate)
        val tv_sign_out = findViewById<TextView>(R.id.tv_sign_out)

        val intent = Intent(this, EditProfileActivity::class.java)

        card_referrer.setOnClickListener {
            intent.putExtra("role", Constants.REFERRER)
            startActivity(intent)
            Toast.makeText(this, "Referrer", Toast.LENGTH_SHORT).show()
        }

        card_candidate.setOnClickListener {
            intent.putExtra("role", Constants.CANDIDATE)
            startActivity(intent)
            Toast.makeText(this, "Candidate", Toast.LENGTH_SHORT).show()
        }

        tv_sign_out.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

            mGoogleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                else {
                    Toast.makeText(this, "Sign out failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tv_enter_code.setOnClickListener {
            val fragment = InviteCodeFragment()
            fragment.show(supportFragmentManager, "")
        }

    }
}