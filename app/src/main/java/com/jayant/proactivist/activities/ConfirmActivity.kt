package com.jayant.proactivist.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.jayant.proactivist.R
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.DialogHelper
import java.util.*

class ConfirmActivity : AppCompatActivity() {

    private val requestPermissionCode = 123
    private lateinit var photo_view: PhotoView
    private lateinit var tv_app_bar: TextView
    private lateinit var iv_back: ImageView
    private lateinit var iv_upload: ImageView
    private lateinit var btn_action: Button
    private var uri: Uri? = null
    private var referrerId = ""
    private var candidateId = ""
    private var applicationId = ""
    private var fromNotification = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm)

        btn_action = findViewById(R.id.btn_action)
        iv_upload = findViewById(R.id.iv_upload)
        tv_app_bar = findViewById(R.id.tv_app_bar)
        iv_back = findViewById(R.id.iv_back)
        photo_view = findViewById(R.id.photo_view)

        intent.extras?.let {
            referrerId = it.getString("ref", "")
            candidateId = it.getString("can", "")
            applicationId = it.getString("application_id", "")
            fromNotification = it.getBoolean("fromNotification", false)
        }

        iv_upload.setOnClickListener {
            fileChooserContract.launch("image/*")
        }
        btn_action.setOnClickListener {
            if(uri == null)
            {
                fileChooserContract.launch("image/*")
            }
            else {
                uploadToFirebase(uri!!)
            }
        }

        tv_app_bar.text = "Confirm Referral"

        iv_back.setOnClickListener {
            if(fromNotification){
                val intent = Intent(this, SplashActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            else {
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if(fromNotification){
            val intent = Intent(this, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        else {
            finish()
        }
    }

    private val fileChooserContract = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        Log.d("Candi", "onActivityResult: uri $uri")
        if(uri != null) {
            btn_action.text = "Upload"
            iv_upload.visibility = View.GONE
            photo_view.visibility = View.VISIBLE
            photo_view.let { Glide.with(this).load(uri).placeholder(R.drawable.ic_upload).into(it) }

            this.uri = uri
        }

    }

    private fun updateStatus(url: String) {
        val database = FirebaseDatabase.getInstance()
        database.reference.child("candidates").child(candidateId).child("applications").child(applicationId).child("status").setValue(Constants.SUBMITTED.toString())
        database.reference.child("referrers").child(referrerId).child("applications").child(applicationId).child("status").setValue(Constants.SUBMITTED.toString())
        database.reference.child("acknowledgments").child(applicationId).child("ref_gid").setValue(referrerId)
        database.reference.child("acknowledgments").child(applicationId).child("can_gid").setValue(candidateId)
        database.reference.child("acknowledgments").child(applicationId).child("screenshot").setValue(url)

        if(fromNotification){
            val intent = Intent(this, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        else {
            val intent = Intent()
            intent.putExtra("confirmed", true)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun uploadToFirebase(uri: Uri): String {
        DialogHelper.showLoadingDialog(this)
        val mStorageRef = FirebaseStorage.getInstance().reference
        val imageUrl = ""
        val storageReference = mStorageRef.child("acknowledgements/$applicationId.jpg")
        val uploadTask = storageReference.putFile(uri)
            .addOnSuccessListener {
                DialogHelper.hideLoadingDialog()
                Toast.makeText(this@ConfirmActivity, "Uploaded", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception -> // Handle unsuccessful uploads
                Toast.makeText(this@ConfirmActivity, exception.toString(), Toast.LENGTH_LONG).show()
                DialogHelper.hideLoadingDialog()

            } as UploadTask
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                DialogHelper.hideLoadingDialog()
                throw task.exception!!
            }

            storageReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val imageUrl = downloadUri.toString()
                updateStatus(imageUrl)
                DialogHelper.hideLoadingDialog()
            }
            else {
                DialogHelper.hideLoadingDialog()
            }
        }
        return imageUrl
    }
}