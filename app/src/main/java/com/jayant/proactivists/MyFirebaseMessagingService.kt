package com.jayant.proactivists

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.jayant.proactivists.activities.ConfirmActivity
import com.jayant.proactivists.activities.HomeActivity
import com.jayant.proactivists.activities.NotificationActivity
import com.jayant.proactivists.models.Confirm
import com.jayant.proactivists.models.Profile
import com.jayant.proactivists.utils.Constants
import com.jayant.proactivists.utils.PrefManager
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        val prefManager = PrefManager(this)
        prefManager.token = p0
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        val json = p0.data
        val type = json["type"]
        val url = json["url"]
        val title = json["title"]
        val message = json["message"]
        val data = json["data"]

        if (type != null) {
            createNotification(title, message, url, type.toInt(), data)
        }
    }

    private fun createNotification(title: String?, subject: String?, imageUri: String?, type: Int?, data: String?) {
        try {
            var intent: Intent? = null
            when(type) {
                Constants.CONFIRMATION_REJECTED -> {
                    val gson = Gson()
                    val itemType = object : TypeToken<Confirm>() {}.type
                    val jsonData = gson.fromJson<Confirm>(data, itemType)
                    intent = Intent(this, ConfirmActivity::class.java)
                    intent.putExtra("ref", jsonData.ref)
                    intent.putExtra("can", jsonData.can)
                    intent.putExtra("application_id", jsonData.application_id)
                    intent.putExtra("fromNotification", true)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                Constants.UPDATE -> {
                    val uri = Uri.parse("https://play.google.com/store/apps/details?id=com.jayant.proactivists")
                    intent = Intent(Intent.ACTION_VIEW, uri)
                }
            }
            val pendingIntent = PendingIntent.getActivity(
                applicationContext, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT
            )
            val channelId = "123"
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.logo)
                    .setStyle(
                        NotificationCompat.BigPictureStyle().bigPicture(getBitmapfromUrl(imageUri))
                    )
                    .setContentTitle(title)
                    .setContentText(subject)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        } catch (e: Exception) {
            Log.e("EXCPETION", "NOTIFICATION $e")
        }
    }

    private fun getBitmapfromUrl(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection =
                url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}