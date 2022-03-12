package com.example.exchangerates.utilities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.exchangerates.R

class NotificationUtil {

    fun makeSyncNotification(message: String, title: String, context: Context, notificationId : Int) {

        // Make a channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name = CHANNEL_NAME
            val description = CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description

            // Add the channel
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            notificationManager?.createNotificationChannel(channel)
        }

        // Create the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

        // Show the notification
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    companion object {
        const val CHANNEL_NAME = "Exchange Rate Channel"
        const val CHANNEL_DESCRIPTION = "Requests to update the list of currencies are made in the background. This notification channel informs you that it is happening"
        const val CHANNEL_ID = "exchange_rates"

        const val CURRENCIES_NOTIFICATION_ID = 1000
    }
}