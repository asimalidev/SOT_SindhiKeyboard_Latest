package com.sindhi.urdu.english.keybad.sindhikeyboard.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.FOFStartActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.NavigationActivity

class FirebaseMessageReceiver : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Extract data from the bundle
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title
        val message = remoteMessage.data["message"] ?: remoteMessage.notification?.body
        val type = remoteMessage.data["type"] ?:"general"
        Log.d("target", "type:$type ")
        showNotification(title, message, type)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("hellofcm", "Refreshed token: $token")

        // IMPORTANT: Send this new token to your backend server
        // so you can continue sending notifications to this device.
    }

    private fun showNotification(title: String?, message: String?, type: String?) {
        val intent = Intent(this, NavigationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("MoveTo", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "keyboard_notify"
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.icon)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setCustomContentView(getCustomDesign(title, message))

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(channelId, "SindhiKeyboard Updates", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun getCustomDesign(title: String?, message: String?): RemoteViews {
        val remoteViews = RemoteViews(packageName, R.layout.notification)
        remoteViews.setTextViewText(R.id.title, title)
        remoteViews.setTextViewText(R.id.message, message)
        remoteViews.setImageViewResource(R.id.icon, R.drawable.icon)
        return remoteViews
    }
}