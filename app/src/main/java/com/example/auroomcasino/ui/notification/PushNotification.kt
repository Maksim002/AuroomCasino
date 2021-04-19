package com.example.auroomcasino.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.auroomcasino.R
import com.example.auroomcasino.ui.main.MainActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class PushNotification : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        val title = p0.notification!!.title.toString()
        val body = p0.notification!!.body.toString()
        showNotification(title, body)
    }

    private fun showNotification(title: String, message: String) {
        FirebaseMessaging.getInstance().subscribeToTopic("com.example.auroomcasino")
        val notChannelId = "com.example.auroomcasino"
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val c = NotificationChannel(notChannelId, "com.example.auroomcasino", NotificationManager.IMPORTANCE_DEFAULT)
            c.description = "auroomcasino"
            c.enableLights(true)
            c.lightColor = Color.BLUE
            notificationManager.createNotificationChannel(c)
        }
        // Create an Intent for the activity you want to start
        val resultIntent: Intent
        //Проверка если токин пустой открой главнй экран иначе переди на нужный экран
        resultIntent = Intent(this, MainActivity::class.java)
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder = NotificationCompat.Builder(this, notChannelId)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_auroom)
            .setAutoCancel(true)
            .setContentIntent(resultPendingIntent)
            .setContentText(message)
            .setOngoing(true)


        val manager = NotificationManagerCompat.from(this)
        manager.notify(1998, builder.build())
    }
}