package com.jayant.proactivist.activities

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.jayant.proactivist.R
import com.jayant.proactivist.utils.Constants
import com.jayant.proactivist.utils.DialogHelper
import pl.aprilapps.easyphotopicker.ChooserType
import pl.aprilapps.easyphotopicker.EasyImage
import java.util.*
import pl.aprilapps.easyphotopicker.MediaSource

import pl.aprilapps.easyphotopicker.MediaFile

import pl.aprilapps.easyphotopicker.DefaultCallback

class ConfirmActivity : AppCompatActivity() {

    private lateinit var easyImage: EasyImage
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
            if (checkPermission()) {
//                fileChooserContract.launch("image/*")
                openImagePicker()
            } else {
                requestPermission()
            }
        }
        btn_action.setOnClickListener {
            if (uri == null) {
                if (checkPermission()) {
//                    fileChooserContract.launch("image/*")
                    openImagePicker()
                } else {
                    requestPermission()
                }
            } else {
                uploadToFirebase(uri!!)
            }
        }

        tv_app_bar.text = "Confirm Referral"

        iv_back.setOnClickListener {
            if (fromNotification) {
                val intent = Intent(this, SplashActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (fromNotification) {
            val intent = Intent(this, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            finish()
        }
    }

    private val fileChooserContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            Log.d("Candi", "onActivityResult: uri $uri")
            if (uri != null) {
                btn_action.text = "Upload"
                iv_upload.visibility = View.GONE
                photo_view.visibility = View.VISIBLE
                photo_view.let {
                    Glide.with(this).load(uri).placeholder(R.drawable.ic_upload).into(it)
                }

                this.uri = uri
            }

        }

    private fun updateStatus(url: String) {
        val database = FirebaseDatabase.getInstance()
        database.reference.child("candidates").child(candidateId).child("applications")
            .child(applicationId).child("status").setValue(Constants.SUBMITTED.toString())
        database.reference.child("referrers").child(referrerId).child("applications")
            .child(applicationId).child("status").setValue(Constants.SUBMITTED.toString())
        database.reference.child("acknowledgments").child(applicationId).child("ref_gid")
            .setValue(referrerId)
        database.reference.child("acknowledgments").child(applicationId).child("can_gid")
            .setValue(candidateId)
        database.reference.child("acknowledgments").child(applicationId).child("screenshot")
            .setValue(url)

        if (fromNotification) {
            val intent = Intent(this, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
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
            } else {
                DialogHelper.hideLoadingDialog()
            }
        }
        return imageUrl
    }

    private fun openImagePicker(){
        easyImage = EasyImage.Builder(this@ConfirmActivity)
            .setChooserType(ChooserType.CAMERA_AND_GALLERY)
            .setCopyImagesToPublicGalleryFolder(true)
            .setFolderName("Proactivist Images")
            .allowMultiple(false)
            .build()
        easyImage.openChooser(this)

    }

    override fun onResume() {
        super.onResume()
        if (checkPermission()) {
            Log.d("StartScreen", "Permissions are there")
        } else {
            requestPermission()
        }
    }


    private fun checkPermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this@ConfirmActivity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            requestPermissionCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestPermissionCode) {
            if (grantResults.isNotEmpty()) {
                val storagePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (storagePermission) {
                    val toast =
                        Toast.makeText(
                            this@ConfirmActivity,
                            "Permission Granted",
                            Toast.LENGTH_SHORT
                        )
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                } else {
                    val toast =
                        Toast.makeText(
                            this@ConfirmActivity,
                            "Permission Denied",
                            Toast.LENGTH_SHORT
                        )
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
//                    showSettingsDialog()
                }
            }
        }
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this@ConfirmActivity)
        builder.setTitle("Need Permissions")
        builder.setCancelable(false)
        builder.setMessage("This app needs audio permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS") { dialog: DialogInterface, which: Int ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, which: Int ->
            dialog.cancel()
            builder.show()
        }
        builder.show()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        easyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
                override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                    Toast.makeText(this@ConfirmActivity, "Photo selected", Toast.LENGTH_SHORT).show()
                    onPhotosReturned(imageFiles)
                }

                override fun onImagePickerError(error: Throwable, source: MediaSource) {
                    error.printStackTrace()
                }

                override fun onCanceled(source: MediaSource) {
                }
            })
    }

    private fun onPhotosReturned(imageFiles: Array<MediaFile>) {
        val uri = imageFiles[0].file.toUri()

        Log.d("Confirm", "onActivityResult: uri $uri")
        btn_action.text = "Upload"
        iv_upload.visibility = View.GONE
        photo_view.visibility = View.VISIBLE
        photo_view.let {
            Glide.with(this).load(uri).placeholder(R.drawable.ic_upload).into(it)
        }

        this.uri = uri

    }

}